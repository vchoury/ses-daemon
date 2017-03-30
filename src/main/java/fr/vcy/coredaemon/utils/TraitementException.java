package fr.vcy.coredaemon.utils;

/**
 *
 * @author vchoury
 */
public class TraitementException extends Exception {
    
    private String filename;
    private String baliseEnErreur;
    private String valeurEnErreur;
    private Integer ligne;
    private Integer colonne;

    public TraitementException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TraitementException(String message, Throwable cause, String filename, String baliseEnErreur, String valeurEnErreur, Integer ligne, Integer colonne) {
        super(message, cause);
        this.filename = filename;
        this.baliseEnErreur = baliseEnErreur;
        this.valeurEnErreur = valeurEnErreur;
        this.ligne = ligne;
        this.colonne = colonne;
    }

    public String getBaliseEnErreur() {
        return baliseEnErreur;
    }

    public Integer getColonne() {
        return colonne;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getLigne() {
        return ligne;
    }

    public String getValeurEnErreur() {
        return valeurEnErreur;
    }

    public void setBaliseEnErreur(String baliseEnErreur) {
        this.baliseEnErreur = baliseEnErreur;
    }

    public void setColonne(Integer colonne) {
        this.colonne = colonne;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setLigne(Integer ligne) {
        this.ligne = ligne;
    }

    public void setValeurEnErreur(String valeurEnErreur) {
        this.valeurEnErreur = valeurEnErreur;
    }
    
}
