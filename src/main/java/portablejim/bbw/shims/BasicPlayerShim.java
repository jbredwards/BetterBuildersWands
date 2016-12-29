package portablejim.bbw.shims;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import portablejim.bbw.BetterBuildersWandsMod;
import portablejim.bbw.basics.Point3d;
import portablejim.bbw.core.items.IWandItem;
import portablejim.bbw.basics.Point3d;
import vazkii.botania.api.item.IBlockProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrap a player to provide basic functions.
 */
public class BasicPlayerShim implements IPlayerShim {
    private EntityPlayer player;
    private boolean providersEnabled;

    protected float assumedReachDistance;

    public BasicPlayerShim(EntityPlayer player) {
        this.player = player;
        this.providersEnabled = areProvidersEnabled();
        this.assumedReachDistance = 4.5F;
    }

    public static Block getBlock(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem());
    }

    public static int getBlockMeta(ItemStack stack) {
        return stack.getHasSubtypes() ? stack.getItemDamage() : 0;
    }

    private static boolean areProvidersEnabled() {
        try {
            boolean disable = new Object() instanceof IBlockProvider;
            return true;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public int countItems(ItemStack itemStack) {
        int total = 0;
        if(itemStack == null || player.inventory == null || player.inventory.mainInventory == null) {
            return 0;
        }

        Block block = getBlock(itemStack);
        int meta = getBlockMeta(itemStack);

        for(ItemStack inventoryStack : player.inventory.mainInventory) {
            if(inventoryStack != null && itemStack.isItemEqual(inventoryStack)) {
                total += Math.max(0, inventoryStack.stackSize);
            }
            else {
                total += BetterBuildersWandsMod.instance.containerManager.countItems(player, itemStack, inventoryStack);
            }
        }

        return itemStack.stackSize > 0 ? total / itemStack.stackSize : 0;
    }

    @Override
    public boolean useItem(ItemStack itemStack) {
        if(itemStack == null || player.inventory == null || player.inventory.mainInventory == null) {
            return false;
        }

        // Reverse direction to leave hotbar to last.
        int toUse = itemStack.stackSize;
        List<ItemStack> providers = new ArrayList<ItemStack>();
        for(int i = player.inventory.mainInventory.length - 1; i >= 0; i--) {
            ItemStack inventoryStack = player.inventory.mainInventory[i];
            if(inventoryStack != null && itemStack.isItemEqual(inventoryStack)) {
                if(inventoryStack.stackSize < toUse) {
                    inventoryStack.stackSize = 0;
                    toUse -= inventoryStack.stackSize;
                }
                else {
                    inventoryStack.stackSize = inventoryStack.stackSize - toUse;
                    toUse = 0;
                }
                if(inventoryStack.stackSize == 0) {
                    player.inventory.setInventorySlotContents(i, null);
                }
                player.inventoryContainer.detectAndSendChanges();
            }
            else {
                toUse = BetterBuildersWandsMod.instance.containerManager.useItems(player, itemStack, inventoryStack, toUse);
            }
            if(toUse <= 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack getNextItem(Block block, int meta) {
        for(int i = player.inventory.mainInventory.length - 1; i >= 0; i--) {
            ItemStack inventoryStack = player.inventory.mainInventory[i];

        }

        return null;
    }

    @Override
    public Point3d getPlayerPosition() {
        return new Point3d((int)player.posX, (int)player.posY, (int)player.posZ);
    }

    @Override
    public EntityPlayer getPlayer() {
        return player;
    }

    @Override
    public ItemStack getHeldWandIfAny() {
        return getHeldWandIfAny(player);
    }

    public static ItemStack getHeldWandIfAny(EntityPlayer player) {
        ItemStack wandItem = null;
        if(player.getHeldItem(EnumHand.MAIN_HAND) != null && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof IWandItem) {
            wandItem = player.getHeldItem(EnumHand.MAIN_HAND);
        }
        else if(player.getHeldItem(EnumHand.OFF_HAND) != null && player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof IWandItem) {
            wandItem = player.getHeldItem(EnumHand.OFF_HAND);
        }
        return wandItem;
    }

    @Override
    public boolean isCreative() {
        return player.capabilities.isCreativeMode;
    }

    @Override
    public double getReachDistance() {
        if(player instanceof EntityPlayerMP) {
            return ((EntityPlayerMP)player).interactionManager.getBlockReachDistance();
        }
        else {
            return this.assumedReachDistance;
        }
    }
}
