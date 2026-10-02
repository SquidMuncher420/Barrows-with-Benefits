package com.barrowswithbenefits;

enum BarrowsBrotherLocationData
{
    // Surface dig rectangles are the complete diggable mound areas.
    // Coordinates are region-local south-west corners in Barrows region 14131.
    DHAROK("Dharok", 20720, 36, 52, 52, 31, 6, 6),
    AHRIM("Ahrim", 20770, 36, 37, 42, 22, 6, 7),
    VERAC("Verac", 20772, 55, 42, 35, 31, 5, 6),
    TORAG("Torag", 20721, 49, 20, 32, 17, 5, 4),
    KARIL("Karil", 20771, 29, 20, 43, 9, 7, 7),
    GUTHAN("Guthan", 20722, 17, 40, 54, 16, 7, 5);

    private final String displayName;
    private final int sarcophagusObjectId;
    private final int cryptRegionX;
    private final int cryptRegionY;
    private final int surfaceRegionX;
    private final int surfaceRegionY;
    private final int surfaceDigWidth;
    private final int surfaceDigHeight;

    BarrowsBrotherLocationData(
        String displayName,
        int sarcophagusObjectId,
        int cryptRegionX,
        int cryptRegionY,
        int surfaceRegionX,
        int surfaceRegionY,
        int surfaceDigWidth,
        int surfaceDigHeight)
    {
        this.displayName = displayName;
        this.sarcophagusObjectId = sarcophagusObjectId;
        this.cryptRegionX = cryptRegionX;
        this.cryptRegionY = cryptRegionY;
        this.surfaceRegionX = surfaceRegionX;
        this.surfaceRegionY = surfaceRegionY;
        this.surfaceDigWidth = surfaceDigWidth;
        this.surfaceDigHeight = surfaceDigHeight;
    }

    String getDisplayName()
    {
        return displayName;
    }

    int getSarcophagusObjectId()
    {
        return sarcophagusObjectId;
    }

    int getCryptRegionX()
    {
        return cryptRegionX;
    }

    int getCryptRegionY()
    {
        return cryptRegionY;
    }

    int getSurfaceRegionX()
    {
        return surfaceRegionX;
    }

    int getSurfaceRegionY()
    {
        return surfaceRegionY;
    }


    int getSurfaceDigWidth()
    {
        return surfaceDigWidth;
    }

    int getSurfaceDigHeight()
    {
        return surfaceDigHeight;
    }

    boolean matchesNpcName(String npcName)
    {
        return npcName != null
            && (npcName.equals(displayName) || npcName.startsWith(displayName + " "));
    }

    static BarrowsBrotherLocationData fromSarcophagusObjectId(int objectId)
    {
        for (BarrowsBrotherLocationData brother : values())
        {
            if (brother.sarcophagusObjectId == objectId)
            {
                return brother;
            }
        }

        return null;
    }
}
