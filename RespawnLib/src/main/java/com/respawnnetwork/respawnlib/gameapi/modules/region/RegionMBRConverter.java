package com.respawnnetwork.respawnlib.gameapi.modules.region;

import org.khelekore.prtree.MBRConverter;


class RegionMBRConverter implements MBRConverter<Region> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMin(int i, Region region) {
        switch (i) {
            case 0:
                return region.getMinimum().x;

            case 1:
                return region.getMinimum().y;

            case 2:
                return region.getMinimum().z;
        }

        return 0;
    }

    @Override
    public double getMax(int i, Region region) {
        switch (i) {
            case 0:
                return region.getMaximum().x;

            case 1:
                return region.getMaximum().y;

            case 2:
                return region.getMaximum().z;
        }

        return 0;
    }

}
