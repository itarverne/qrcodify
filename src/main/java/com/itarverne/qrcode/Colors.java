package com.itarverne.qrcode;

/**
 * The available color for QRCode background
 */
public enum Colors {
	
	BLUE(0xFF40BAD0),
    RED(0xFFE91C43),
    PURPLE(0xFF8A4F9E),
    ORANGE(0xFFF4B13D),
    WHITE(0xFFFFFFFF),
    BLACK(0xFF000000);

    private final int argb;

    Colors(final int argb){
        this.argb = argb;
    }

    public int getArgb(){
        return argb;
    }
}
