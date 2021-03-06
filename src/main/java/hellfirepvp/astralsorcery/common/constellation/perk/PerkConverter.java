/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2018
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.perk;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.constellation.perk.attribute.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.util.data.Tuple;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkConverter
 * Created by HellFirePvP
 * Date: 02.08.2018 / 23:12
 */
public abstract class PerkConverter {

    private static int counter = 0;

    private final int id;

    public PerkConverter() {
        this.id = counter;
        counter++;
    }

    public static final PerkConverter IDENTITY = new PerkConverter() {
        @Nonnull
        @Override
        public PerkAttributeModifier convertModifier(PerkAttributeModifier perkAttributeModifier, @Nullable AbstractPerk owningPerk) {
            return perkAttributeModifier;
        }
    };

    /**
     * Use {@link PerkAttributeModifier#convertModifier(String, PerkAttributeModifier.Mode, float)} to convert the given modifier
     */
    @Nonnull
    public abstract PerkAttributeModifier convertModifier(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk);

    /**
     * Use {@link PerkAttributeModifier#gainAsExtraModifier(PerkConverter, String, PerkAttributeModifier.Mode, float)} to create new modifiers
     * based off of the given modifier! The resulting modifiers cannot be modified with perk converters.
     */
    @Nonnull
    public Collection<PerkAttributeModifier> gainExtraModifiers(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
        return Lists.newArrayList();
    }

    public void onApply(EntityPlayer player, Side side) {}

    public void onRemove(EntityPlayer player, Side side) {}

    public PerkConverter andThen(PerkConverter next) {
        PerkConverter thisConverter = this;
        return new PerkConverter() {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifier(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
                return thisConverter.convertModifier(next.convertModifier(modifier, owningPerk), owningPerk);
            }
        };
    }

    public Radius asRangedConverter(Point.Double offset, double radius) {
        PerkConverter thisConverter = this;
        return new Radius(offset, radius) {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifierInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
                return thisConverter.convertModifier(modifier, owningPerk);
            }

            @Nonnull
            @Override
            public Collection<PerkAttributeModifier> gainExtraModifiersInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
                return thisConverter.gainExtraModifiers(modifier, owningPerk);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerkConverter that = (PerkConverter) o;
        return id == that.id;
    }

    public static abstract class Radius extends PerkConverter {

        private final double radius;
        private final Point.Double offset;

        public Radius(Point.Double point, double radius) {
            this.offset = point;
            this.radius = radius;
        }

        public double getRadius() {
            return radius;
        }

        public Point.Double getOffset() {
            return offset;
        }

        public Radius withNewRadius(double radius) {
            Radius thisRadius = this;
            return new Radius(thisRadius.getOffset(), radius) {
                @Nonnull
                @Override
                public PerkAttributeModifier convertModifierInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
                    return thisRadius.convertModifierInRange(modifier, owningPerk);
                }

                @Nonnull
                @Override
                public Collection<PerkAttributeModifier> gainExtraModifiersInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
                    return thisRadius.gainExtraModifiersInRange(modifier, owningPerk);
                }
            };
        }

        protected boolean canAffectPerk(@Nullable AbstractPerk otherPerk) {
            return otherPerk != null && getOffset().distance(otherPerk.getOffset()) <= getRadius();
        }

        @Nonnull
        @Override
        public PerkAttributeModifier convertModifier(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
            if (!canAffectPerk(owningPerk)) {
                return modifier;
            }
            return convertModifierInRange(modifier, owningPerk);
        }

        @Nonnull
        @Override
        public Collection<PerkAttributeModifier> gainExtraModifiers(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk) {
            if (!canAffectPerk(owningPerk)) {
                return Collections.emptyList();
            }
            return gainExtraModifiersInRange(modifier, owningPerk);
        }

        @Nonnull
        public abstract PerkAttributeModifier convertModifierInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk);

        @Nonnull
        public abstract Collection<PerkAttributeModifier> gainExtraModifiersInRange(PerkAttributeModifier modifier, @Nullable AbstractPerk owningPerk);

    }

}
