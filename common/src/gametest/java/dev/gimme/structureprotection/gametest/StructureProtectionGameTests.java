package dev.gimme.structureprotection.gametest;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.gimme.structureprotection.application.BlockProtection;
import dev.gimme.structureprotection.domain.BlockEdit;
import dev.gimme.structureprotection.domain.IdPattern;
import dev.gimme.structureprotection.domain.StructureRule;
import dev.gimme.structureprotection.domain.StructureSource;
import dev.gimme.structureprotection.domain.StructureSource.Match;
import dev.gimme.structureprotection.domain.config.ServerConfig;
import dev.gimme.structureprotection.infrastructure.ConfigTestSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Loader-agnostic game test bodies. Each {@code static void(GameTestHelper)} method is one test;
 * a test passes by calling {@link GameTestHelper#succeed()} and fails by throwing.
 *
 * <p>To add a test: write the method here, then wire it into {@code FabricGameTests} and
 * {@code NeoForgeGameTests}.
 */
public final class StructureProtectionGameTests {

    private StructureProtectionGameTests() {
    }

    /**
     * The shared {@link ServerConfig} is initialized by the loader and exposes its documented defaults,
     * exercising the cross-loader Forge Config API Port + night-config TOML parsing path on either loader.
     */
    public static void configDefaultsLoaded(GameTestHelper helper) {
        ServerConfig config = ServerConfig.INSTANCE;
        helper.assertTrue(config != null, "ServerConfig should be initialized by the loader");

        List<StructureRule> rules = config.getStructureRules();
        helper.assertFalse(rules.isEmpty(), "the default config should define at least one structure rule");
        helper.assertTrue(rules.stream().anyMatch(StructureRule::protectStructural),
                "the defaults should protect at least one structure's shape (protectStructural)");
        helper.assertTrue(rules.stream().anyMatch(rule -> rule.protectStructural() && !rule.breachable()),
                "the defaults should include an always-protected (non-breachable) structure");
        helper.assertTrue(rules.stream().anyMatch(StructureRule::breachable),
                "the defaults should include a breachable structure (e.g. stronghold)");
        helper.assertTrue(rules.stream().anyMatch(rule ->
                        rule.protect().isEmpty() && !rule.protectStructural()
                                && !rule.canBreak().isEmpty()),
                "the defaults should include a non-protecting base rule contributing a canBreak allow-list");
        helper.assertTrue(rules.stream().filter(StructureRule::protectStructural)
                        .allMatch(rule -> rule.canPlace().isEmpty() && rule.canBreak().isEmpty()),
                "the protecting rules should carry no own allow-lists (exceptions live on the shared base)");
        helper.succeed();
    }

    /**
     * Config values are live and per-structure: a change to a rule's {@code canBreak} is reflected through
     * {@link ServerConfig}, confirming the test-support handle binds to the loaded spec on either loader.
     */
    public static void protectedStructuresConfigurable(GameTestHelper helper) {
        List<? extends Config> original = ConfigTestSupport.STRUCTURE_PROTECTION.get();
        try {
            Config rule = TomlFormat.newConfig();
            rule.set("structures", "minecraft:stronghold");
            rule.set("breachable", true);
            rule.set("canBreak", "obsidian");

            ConfigTestSupport.STRUCTURE_PROTECTION.set(List.of(rule));

            List<StructureRule> rules = ServerConfig.INSTANCE.getStructureRules();
            helper.assertTrue(rules.size() == 1, "expected exactly one configured rule but got " + rules.size());
            StructureRule configured = rules.getFirst();
            helper.assertTrue(configured.breachable(), "configured rule should be breachable");
            helper.assertTrue("obsidian".equals(configured.canBreak().raw()),
                    "canBreak should follow the configured value but was " + configured.canBreak().raw());
            helper.assertFalse(configured.protectStructural(),
                    "a rule with no protectStructural key should default to false");
            helper.assertTrue(configured.protect().isEmpty(),
                    "a rule with no protect key should default to protecting nothing");
        } finally {
            ConfigTestSupport.STRUCTURE_PROTECTION.set(original);
        }
        helper.succeed();
    }

    /**
     * The optional {@code protectStructural} flag and {@code protect} regex round-trip through {@link ServerConfig}:
     * present values are honored, and absent keys fall back to the documented defaults (false / empty).
     */
    public static void protectStructuralConfigurable(GameTestHelper helper) {
        List<? extends Config> original = ConfigTestSupport.STRUCTURE_PROTECTION.get();
        try {
            Config structural = TomlFormat.newConfig();
            structural.set("structures", "minecraft:ancient_city");
            structural.set("protectStructural", true);

            Config full = TomlFormat.newConfig();
            full.set("structures", "minecraft:fortress");
            full.set("protect", ".*");

            ConfigTestSupport.STRUCTURE_PROTECTION.set(List.of(structural, full));

            List<StructureRule> rules = ServerConfig.INSTANCE.getStructureRules();
            helper.assertTrue(rules.size() == 2, "expected exactly two configured rules but got " + rules.size());
            helper.assertTrue(rules.getFirst().protectStructural(),
                    "a rule with protectStructural=true should report it");
            helper.assertTrue(rules.getFirst().protect().isEmpty(),
                    "a rule with no protect key should default to empty");
            helper.assertFalse(rules.get(1).protectStructural(),
                    "a rule with no protectStructural key should default to false");
            helper.assertTrue(".*".equals(rules.get(1).protect().raw()),
                    "the protect regex should follow the configured value but was " + rules.get(1).protect().raw());
        } finally {
            ConfigTestSupport.STRUCTURE_PROTECTION.set(original);
        }
        helper.succeed();
    }

    /**
     * The policy logic on {@link StructureRule} and {@link IdPattern} reads the way the docs describe, exercised directly
     * (no world needed): {@code appliesTo} honors colon-vs-path matching, {@code protects} combines the block pattern
     * with {@code protectStructural} + structural-ness, and the allow-lists override per edit kind.
     */
    public static void rulePolicy(GameTestHelper helper) {
        Identifier fortress = Identifier.fromNamespaceAndPath("minecraft", "fortress");
        Identifier endCity = Identifier.fromNamespaceAndPath("minecraft", "end_city");
        Identifier spawner = Identifier.fromNamespaceAndPath("minecraft", "spawner");
        Identifier stone = Identifier.fromNamespaceAndPath("minecraft", "stone");
        Identifier pot = Identifier.fromNamespaceAndPath("minecraft", "decorated_pot");

        // A shape rule (path regex) that still lets players break decorated pots to loot them.
        StructureRule shape = new StructureRule(
                IdPattern.of("fortress"), IdPattern.NONE, true, false,
                IdPattern.NONE, IdPattern.of("decorated_pot"));

        helper.assertTrue(shape.appliesTo(fortress), "a path regex should match the fortress id");
        helper.assertFalse(shape.appliesTo(endCity), "a fortress rule should not apply to end_city");
        helper.assertTrue(shape.protects(stone, true), "protectStructural should protect a structural block");
        helper.assertFalse(shape.protects(spawner, false),
                "protectStructural should leave a non-structural block editable");
        helper.assertTrue(shape.allowsBreaking(pot), "canBreak should allow breaking the decorated pot");
        helper.assertFalse(shape.allowsPlacing(pot), "an empty canPlace should allow nothing");

        // A targeted rule that protects one named block, matched against the full namespaced id.
        StructureRule named = new StructureRule(
                IdPattern.of("minecraft:fortress"), IdPattern.of("spawner"), false, false,
                IdPattern.NONE, IdPattern.NONE);

        helper.assertTrue(named.appliesTo(fortress), "a namespaced regex should match the full id");
        helper.assertTrue(named.protects(spawner, false), "the protect regex should protect the named block");
        helper.assertFalse(named.protects(stone, true), "the protect regex should not protect an unlisted block");

        // The empty pattern matches nothing.
        helper.assertFalse(IdPattern.NONE.matches(stone), "the empty pattern should match nothing");
        helper.assertTrue(IdPattern.NONE.isEmpty(), "the empty pattern should report empty");

        helper.succeed();
    }

    /**
     * A block's structural-ness is a property of its type, judged by its default state, identically for placing and
     * breaking. Solid blocks are structural; non-physical decoration is not; and a fence gate is structural by its
     * default (closed) state, even though an open gate blocks no motion — so it cannot be opened to dodge shape
     * protection. {@code BlockEdit} enforces this by construction: it holds a block, never a particular block state.
     */
    public static void blockEditStructural(GameTestHelper helper) {
        helper.assertTrue(BlockEdit.breaking(Blocks.STONE).isStructural(),
                "a solid block should be structural");
        helper.assertFalse(BlockEdit.breaking(Blocks.TORCH).isStructural(),
                "a non-physical decoration block should not be structural");
        helper.assertTrue(BlockEdit.breaking(Blocks.OAK_FENCE_GATE).isStructural(),
                "a fence gate should be structural by its default (closed) state, so it can't be opened to dodge it");

        // Placing and breaking agree: structural-ness is a property of the block, not the edit kind.
        helper.assertTrue(BlockEdit.placing(Blocks.STONE).isStructural() == BlockEdit.breaking(Blocks.STONE).isStructural(),
                "placing and breaking should judge structural-ness identically");

        BlockEdit place = BlockEdit.placing(Blocks.STONE);
        helper.assertTrue(place.isPlacing() && !place.isBreaking(), "a placing edit should report placing");
        BlockEdit breakEdit = BlockEdit.breaking(Blocks.STONE);
        helper.assertTrue(breakEdit.isBreaking() && !breakEdit.isPlacing(), "a breaking edit should report breaking");

        helper.succeed();
    }

    /**
     * The break decision in {@link BlockProtection}, driven against a fake {@link StructureSource} so each scenario can
     * be set up exactly: creative bypass, protection by shape and by name, the {@code canBreak} allow-list, breach
     * (locked inside / breakable from outside), and the union of rules across a structure.
     */
    public static void blockProtectionBreak(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Identifier structure = Identifier.fromNamespaceAndPath("minecraft", "stronghold");

        // A real block to break: stone is structural (its default state blocks motion).
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above();
        level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());

        Player survival = helper.makeMockPlayer(GameType.SURVIVAL);
        Player creative = helper.makeMockPlayer(GameType.CREATIVE);

        FakeSource source = new FakeSource();
        BlockProtection protection = new BlockProtection(source);

        // Outside any structure, nothing is protected.
        source.set(List.of(), false);
        helper.assertFalse(protection.preventsBreak(level, pos, survival),
                "a block outside any structure should be breakable");

        // protectStructural protects stone (structural).
        source.set(match(structure, structural(false)), false);
        helper.assertTrue(protection.preventsBreak(level, pos, survival),
                "a structural block in a protected structure should be unbreakable");

        // Creative bypasses protection.
        helper.assertFalse(protection.preventsBreak(level, pos, creative),
                "creative mode should bypass protection");

        // A protect regex that doesn't match stone leaves it editable; one that does protects it.
        source.set(match(structure, byName("diamond_block")), false);
        helper.assertFalse(protection.preventsBreak(level, pos, survival),
                "a protect regex that doesn't match the block should not protect it");
        source.set(match(structure, byName("stone")), false);
        helper.assertTrue(protection.preventsBreak(level, pos, survival),
                "a protect regex matching the block should protect it");

        // canBreak overrides protection.
        source.set(match(structure, rule(true, false, "", "stone")), false);
        helper.assertFalse(protection.preventsBreak(level, pos, survival),
                "a canBreak allow-list should permit breaking the block");

        // Breachable: locked while inside, breakable from outside.
        source.set(match(structure, structural(true)), true);
        helper.assertTrue(protection.preventsBreak(level, pos, survival),
                "a breachable structure should stay locked while inside it");
        source.set(match(structure, structural(true)), false);
        helper.assertFalse(protection.preventsBreak(level, pos, survival),
                "a breachable structure should be breakable from outside");

        // A non-breachable structure stays protected even from outside.
        source.set(match(structure, structural(false)), false);
        helper.assertTrue(protection.preventsBreak(level, pos, survival),
                "a non-breachable structure should stay protected from outside");

        // Union: a protecting rule plus a non-protecting "library" rule whose canBreak allows the block.
        source.set(match(structure, structural(false), rule(false, false, "", "stone")), false);
        helper.assertFalse(protection.preventsBreak(level, pos, survival),
                "a canBreak allow-list on any matching rule should override protection (union)");

        helper.succeed();
    }

    /**
     * The place decision in {@link BlockProtection}: breaching only ever permits breaking a way in, never placing, and
     * the place/break allow-lists are kept separate ({@code canPlace} permits placing; {@code canBreak} does not).
     */
    public static void blockProtectionPlace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Identifier structure = Identifier.fromNamespaceAndPath("minecraft", "stronghold");
        Player survival = helper.makeMockPlayer(GameType.SURVIVAL);

        // A placement context for a stone block (structural).
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        // The 4-arg ctor derives the level from the player; the mock player's level is this test level.
        BlockPlaceContext place = new BlockPlaceContext(
                survival, InteractionHand.MAIN_HAND, new ItemStack(Blocks.STONE), hit);

        FakeSource source = new FakeSource();
        BlockProtection protection = new BlockProtection(source);

        // Even outside a breachable structure, placing is blocked — breach only ever lets you break in.
        source.set(match(structure, structural(true)), false);
        helper.assertTrue(protection.preventsPlace(level, place),
                "breach should permit breaking in, never placing");

        // canPlace permits placing; canBreak does not.
        source.set(match(structure, rule(true, false, "stone", "")), false);
        helper.assertFalse(protection.preventsPlace(level, place),
                "a canPlace allow-list should permit placing the block");
        source.set(match(structure, rule(true, false, "", "stone")), false);
        helper.assertTrue(protection.preventsPlace(level, place),
                "a canBreak allow-list should not permit placing");

        helper.succeed();
    }

    private static List<Match> match(Identifier structure, StructureRule... rules) {
        return List.of(new Match(structure, List.of(rules)));
    }

    private static StructureRule structural(boolean breachable) {
        return rule(true, breachable, "", "");
    }

    private static StructureRule byName(String protect) {
        return new StructureRule(IdPattern.of(".*"), IdPattern.of(protect), false, false, IdPattern.NONE, IdPattern.NONE);
    }

    private static StructureRule rule(boolean protectStructural, boolean breachable, String canPlace, String canBreak) {
        return new StructureRule(IdPattern.of(".*"), IdPattern.NONE, protectStructural, breachable,
                IdPattern.of(canPlace), IdPattern.of(canBreak));
    }

    /** A {@link StructureSource} whose verdicts are set per scenario, independent of any real world structure. */
    private static final class FakeSource implements StructureSource {
        private List<Match> matches = List.of();
        private boolean inside;

        void set(List<Match> matches, boolean inside) {
            this.matches = matches;
            this.inside = inside;
        }

        @Override
        public List<Match> matchesAt(Level level, BlockPos pos) {
            return matches;
        }

        @Override
        public boolean isInsidePiece(Level level, BlockPos pos, Identifier structure) {
            return inside;
        }
    }
}
