package model;

/**
 * A class for helping TextDao to organize the structure of
 * the files in the database and get their context.
 */
public class Document {

    private String docName;
    private String docText;

    /**
     * @param docName The md5 code of the file
     * @param docText The context of the file
     */
    public Document(String docName, String docText){
        this.docName = docName;
        this.docText = docText;
    }

    public String getName() {
        return docName;
    }

    public String getText() {
        return docText;
    }

    public void setName(String docName) {
        this.docName = docName;
    }

    public void setText(String docText) {
        this.docText = docText;
    }

}
