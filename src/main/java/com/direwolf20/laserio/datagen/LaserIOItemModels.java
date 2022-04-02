package com.direwolf20.laserio.datagen;

import com.direwolf20.laserio.common.LaserIO;
import com.direwolf20.laserio.setup.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LaserIOItemModels extends ItemModelProvider {
    public LaserIOItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, LaserIO.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Block Items
        withExistingParent(Registration.LaserConnector_ITEM.get().getRegistryName().getPath(), modLoc("block/laser_connector"));
        withExistingParent(Registration.LaserNode_ITEM.get().getRegistryName().getPath(), modLoc("block/laser_node"));

        //Item items
        singleTexture(Registration.Laser_Wrench.get().getRegistryName().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/laser_wrench"));
        singleTexture(Registration.Card_Item.get().getRegistryName().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_item"));
        singleTexture(Registration.Card_Fluid.get().getRegistryName().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_fluid"));
        singleTexture(Registration.Card_Energy.get().getRegistryName().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/card_energy"));

    }
}