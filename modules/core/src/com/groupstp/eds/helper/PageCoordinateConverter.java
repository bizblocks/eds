package com.groupstp.eds.helper;

import com.itextpdf.text.Rectangle;

/**
 * Helper class for converting convenient coordinates
 * (left-up-x, left-up-y, width, height) to {@link Rectangle}
 * diagonal coordinates (llx, lly, urx, ury)
 */
public class PageCoordinateConverter {
    private Rectangle pageSize;

    public PageCoordinateConverter() {
    }

    public PageCoordinateConverter(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Convert coordinates and return {@link Rectangle}
     * @param lux left upper x
     * @param luy left upper y
     * @param width rectangle width
     * @param height rectangle height
     * @return - {@link Rectangle}
     */
    public Rectangle getRectangle(int lux, int luy, int width, int height){
        final float llx = lux;
        final float lly = pageSize.getTop() - luy - height;
        final float urx = lux + width;
        final float ury = pageSize.getTop() - luy;

        return new Rectangle(llx, lly, urx, ury);
    }

}
