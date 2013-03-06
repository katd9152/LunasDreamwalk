package equestria.canterlot.lunasdreamwalk.util;

import java.io.IOException;
import java.util.Arrays;

/**
 * (More or less) transparently convert raw savegame data to String or back to
 * raw data
 * 
 */
public class SaveFileAdapter {

    private byte[] rawData;
    private byte[] decompressedContent;
    private String xmlContent;

    private byte[] firstKey;
    private byte[] secondKey;

    public SaveFileAdapter(byte[] fileContent) throws IOException {
        this.rawData = fileContent;

        // This key is derived in a complicated way by combining content of
        // two images and other shenanigans. Because it is identical on all
        // installations of the game, I just use the actual key and skip its
        // calculation
        this.setSecondKey(Util.hexStringToByteArray("302A75507ACBD72F89504E4712901CF0"));
    }

    public void setFirstKey(byte[] firstKey) {
        this.firstKey = firstKey;
    }

    private void setSecondKey(byte[] secondKey) {
        this.secondKey = secondKey;
    }

    public int getRawDataSize() {
        return rawData.length;
    }

    private int payloadDecompressedSize() {
        return Util.parseInteger(rawData, 0);
    }

    private int payloadCompressedSize() {
        return Util.parseInteger(rawData, 4);
    }

    private int payloadEncryptedSize() {
        return Util.parseInteger(rawData, 8);
    }

    private byte[] payload() {
        return Arrays.copyOfRange(rawData, 12, rawData.length - 4);
    }

    private int getVersionNumber() {
        return Util.parseInteger(rawData, rawData.length - 4);
    }

    /**
     * Quick test if the data "adds up", before doing any serious work on the
     * data.
     * 
     */
    public boolean sanityCheck() {
        return getVersionNumber() == 1 && payloadDecompressedSize() > 0 && payloadCompressedSize() > 0 && payloadEncryptedSize() == payload().length;
    }

    /**
     * Unwrap the outer, older layer based on IMEI/GLUID encryption and the
     * useless compression. You can't compress encrypted data *facehoof*
     * 
     * This is also the reason why savegames have massively grown in size after
     * Gameloft changed the protection scheme. They are now essentially not
     * compressed at all.
     */
    public boolean decryptFirstLayer() {
        byte[] decrypted = Util.decrypt(payload(), firstKey);

        byte[] decompressed;

        try {
            decompressed = Util.decompress(decrypted);
        } catch(Exception e) {
            return false;
        }

        this.decompressedContent = decompressed;
        return true;
    }

    /**
     * Unwrap the inner, newer layer based on the encryption with the super
     * secret, well hidden obscure key that shows how much fun some devs at
     * Gameloft had.
     * 
     */
    public boolean decryptSecondLayer() {
        if(this.decompressedContent == null) {
            return false;
        }

        byte[] decrypted = Util.decrypt(this.decompressedContent, secondKey);

        String xmlContent = Util.ASCIIByteArrayToString(decrypted);

        this.xmlContent = xmlContent;

        return true;
    }

    public void setXMLContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    /**
     * Wrap the inner layer based on the encryption with the super secret, well
     * hidden obscure key that shows how much fun some devs at Gameloft had.
     * 
     */
    public boolean encryptSecondLayer() {
        if(this.xmlContent == null) {
            return false;
        }

        byte[] encrypted = Util.encrypt(Util.stringToASCIIByteArray(this.xmlContent.toString()), secondKey);

        this.decompressedContent = encrypted;
        return true;
    }

    /**
     * Wrap the outer, older layer based on IMEI/GLUID encryption and the
     * useless compression. You can't compress encrypted data *facehoof*
     * 
     */
    public boolean encryptFirstLayer() {

        // first calculate checksum - will be needed later
        byte[] crc32 = Util.reorderBytes(Util.CRC32(this.decompressedContent));
        byte[] compressed;
        try {
            compressed = Util.compress(this.decompressedContent);
        } catch(Exception e) {
            return false;
        }

        byte[] newCompressed = new byte[compressed.length + 8 - (compressed.length + 4) % 4];

        System.arraycopy(compressed, 0, newCompressed, 0, compressed.length);
        System.arraycopy(crc32, 0, newCompressed, compressed.length, 4);

        byte[] encrypted = Util.encrypt(newCompressed, firstKey);

        // Now build the full file content
        this.rawData = new byte[encrypted.length + 16];

        Util.writeInteger(this.rawData, 0, this.decompressedContent.length);
        Util.writeInteger(this.rawData, 4, compressed.length + 4);
        Util.writeInteger(this.rawData, 8, encrypted.length);
        System.arraycopy(encrypted, 0, this.rawData, 12, encrypted.length);
        Util.writeInteger(this.rawData, this.rawData.length - 4, 1);

        return true;

    }

    public byte[] fileContent() {
        return this.rawData;
    }

    public String getXMLContent() {
        return this.xmlContent;
    }
}
