package model;

/**
 * A class especially build for the TextDao.List to quickly reach to
 * the md5 code, context length and the preview of the file.
 */
public class DocumentAbstract {
    /**
     * the md5 code of the file
     */
    private String md5;
    /**
     * the length of the file
     */
    private int length;
    /**
     * the preview of the file text (within 100 words)
     */
    private String preview;

    /**
     * This constructor will use the normal structure of the file to
     * generate the file length and the preview of the file.
     * @param doc The normal structure of the file.
     */
    public DocumentAbstract(Document doc){
        this.md5 = doc.getName();
        this.length = doc.getText().length();
        if (length < 101) {
            preview = doc.getText();
        }else {
            preview = doc.getText().substring(0,99);
        }
    }

    public int getLength() {
        return length;
    }

    public String getMd5() {
        return md5;
    }

    public String getPreview() {
        return preview;
    }
}
