package by.cascade.chatcot.storage.databaseprocessing.phrases.xml;

import by.cascade.chatcot.storage.databaseprocessing.phrases.PhraseAdapter;
import by.cascade.chatcot.storage.databaseprocessing.phrases.PhraseModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static by.cascade.chatcot.storage.databaseprocessing.phrases.xml.XmlConstants.*;

/**
 * XmlAdapter - class for working with phrases by using XML file
 */
public class XmlAdapter implements PhraseAdapter {
    private static final Logger LOGGER = LogManager.getLogger(XmlAdapter.class);

    private PhraseParsable parser;

    private String path;

    private static final String prefix = "temp";


    /**
     * Create XmlAdapter for working with phrases by using XML file
     * @param path - path of the XML file
     * @param parser - used parser
     */
    public XmlAdapter(String path, PhraseParsable parser) {
        this.path = path;
        this.parser = parser;
    }

    /**
     * adding new phrase into XML file
     * @param phrase - phrase for inserting
     * @param index - index to insert
     */
    public void add(PhraseModel phrase, int index) {
        LOGGER.info("adding " + phrase.toString() + " to XML file to index = " + index);
        PhraseModel phrases[] = parser.parseFromXML(path);
        if (phrases != null) {
            if (index < 0 || index > phrases.length) {
                LOGGER.info("invalid index");
                return;
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(path, false);
            LOGGER.info("adding begin of XML file");
            fileWriter.write(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLBEGIN));
            if (phrases != null && index <= phrases.length) {
                for (int i = 0; i < index; i++) {
                    fileWriter.append(PhraseXmlAdapter.parseToXML(phrases[i], XMLNONE));
                }
            }
            if (phrases != null) {
                LOGGER.info("setting id = " + phrases.length);
                phrase.setId(phrases.length);
            }
            else {
                LOGGER.info("setting id = 0");
                phrase.setId(0);
            }
            fileWriter.append(PhraseXmlAdapter.parseToXML(phrase, XMLNONE));
            if (phrases != null && index < phrases.length) {
                for (int i = index; i < phrases.length; i++) {
                    fileWriter.append(PhraseXmlAdapter.parseToXML(phrases[i], XMLNONE));
                }
            }
            LOGGER.info("adding end of XML file");
            fileWriter.append(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLEND));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * adding new phrase at begin of XML file
     * @param phrase - phrase for inserting
     */
    public void addInBegin(PhraseModel phrase) {
        LOGGER.info("adding in begin");
        add(phrase, 0);
    }

    /**
     * adding new phrase at end of XML file
     * @param phrase - phrase for inserting
     */
    public void addInEnd(PhraseModel phrase) {
        LOGGER.info("adding in end");
        if (parser.parseFromXML(path) != null) {
            this.add(phrase, parser.parseFromXML(path).length);
        } else {
            this.add(phrase, 0);
        }
    }

    /**
     * deleting phrase by index from XML file
     * @param index - index of deleting phrase in XML file
     * @return - true - if deleting was completed
     *         - false - if deleting wasn't completed
     */
    public boolean delete(int index) {
        try {
            LOGGER.info("deleting from index = " + index);
            PhraseModel phrases[] = parser.parseFromXML(path);
            if (phrases != null && index >= 0 && index < phrases.length) {
                FileWriter fileWriter = new FileWriter(prefix + path, false);
                fileWriter.write(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLBEGIN));
                if (index >= phrases.length) {
                    return false;
                }
                for (int i = 0; i < index; i++) {
                    fileWriter.append(PhraseXmlAdapter.parseToXML(phrases[i], XMLNONE));
                }
                for (int i = index + 1; i < phrases.length; i++) {
                    fileWriter.append(PhraseXmlAdapter.parseToXML(phrases[i], XMLNONE));
                }
                fileWriter.append(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLEND));
                fileWriter.flush();
                fileWriter.close();

                LOGGER.info("create temp file");
                File file = new File(prefix + path);
                File oldFile = new File(path);
                if (oldFile.delete()) {
                    if (!file.renameTo(oldFile)) {
                        LOGGER.error("Can't rename file");
                    }
                } else {
                    LOGGER.error("Can't delete file");
                }
                return true;
            } else {
                if (phrases == null) {
                    File file = new File(path);
                    LOGGER.info("delete file");
                    file.delete();
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    /**
     * editing phrase in XML file
     * @param phrase - new phrase from editing
     * @param index - index to edit
     */
    public void edit(PhraseModel phrase, int index) {
        LOGGER.info("edit index = " + index);
        if (delete(index)) {
            add(phrase, index);
        }
    }


    /**
     * getting phrase from index in XML file
     * @param index - index of phrase in XML file
     * @return - selected phrase
     */
    public PhraseModel get(int index) {
        return parser.parseFromXML(path, index);
    }


    /**
     * getting list of phrases
     * @return - list of all phrases from XML file
     */
    public LinkedList<PhraseModel> getList() {
        LOGGER.info("getting list from XML");
        LinkedList<PhraseModel> list = new LinkedList<PhraseModel>();
        PhraseModel phrase;
        int index = 0;
        do {
            phrase = parser.parseFromXML(path, index++);
            if (phrase != null) {
                list.add(phrase);
            }
        } while (phrase != null);
        return list;
    }


    /**
     * find phrases by value
     * @param otherPhrase - value of phrase
     * @return - index of phrase into XML file
     */
    public int findIndexByPhrase(String otherPhrase) {
        LOGGER.info("finding by phrase = \"" + otherPhrase + "\"");
        PhraseModel phrase;
        int index = 0;
        do {
            phrase = parser.parseFromXML(path, index);
            if (phrase != null) {
                if (phrase.getPhrase().equals(otherPhrase)) {
                    LOGGER.info("find = " + index);
                    return index;
                }
            }
            index++;
        } while (phrase != null);
        LOGGER.info("not founded");
        return -1;
    }

    /**
     * find phrase by type
     * @param otherType - type of phrase
     * @return - first founded phrase with this type
     */
    public int findIndexByType(String otherType) {
        LOGGER.info("find by type = \"" + otherType + "\"");
        PhraseModel phrase;
        int index = 0;
        do {
            phrase = parser.parseFromXML(path, index);
            if (phrase != null) {
                if (phrase.getType().equals(otherType)) {
                    LOGGER.info("find = " + index);
                    return index;
                }
            }
            index++;
        } while (phrase != null);
        LOGGER.info("not founded");
        return -1;
    }


    /**
     * getting length of XML file
     * @return - count of phrases into XML file
     */
    public int length() {
        PhraseModel[] phrases = parser.parseFromXML(path);
        if (phrases != null) {
            return phrases.length;
        } else {
            return 0;
        }
    }

    /**
     * transform all phrases of XML file into String
     * @return - all phrases of XML file
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        PhraseModel[] phrases = parser.parseFromXML(path);
        if (phrases != null) {
            for (PhraseModel phrase : parser.parseFromXML(path)) {
                if (phrase != null) {
                    s.append(phrase.toString());
                }
            }
        }
        return s.toString();
    }

    /**
     * getting path of current XML file
     * @return - path of XML file
     */
    public String getPath() {
        return path;
    }

    /**
     * setting path of XML file
     * @param path - path of XML file
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * setting parser for working with XML file
     * @param parser - Object, that implements Parserable
     */
    public void setParser(PhraseParsable parser) {
        this.parser = parser;
    }


    /**
     * adding new phrase into XML file
     * @param type - type
     * @param phrase - phrase
     */
    @Override
    public void addPhrase(String type, String phrase, int owner) {
        LOGGER.info("adding new phrase : (type = \"" + type + "\", phrase = \"" + phrase + "\")");
        addInEnd(new PhraseModel(type, phrase, owner));
    }

    /**
     * getting all phrases from XML file
     * @return - list of all phrases
     */
    @Override
    public List<PhraseModel> listPhrases() {
        LOGGER.info("getting list of phrases");
        return getList();
    }

    /**
     * deleting all phrases in list from XML file
     * @param list - list of phrases to deleting
     */
    @Override
    public void deletePhrases(List<PhraseModel> list) {
        LOGGER.info("delete list");
        for (PhraseModel model : list) {
            delete(findIndexByPhrase(model.getPhrase()));
        }
    }

    /**
     * deleting by id from XML file
     * @param model - model with id
     */
    @Override
    public void deleteId(PhraseModel model) {
        delete(model.getId());
    }

    /**
     * deleting by phrase from XML file
     * @param model - model with phrase
     */
    @Override
    public void deletePhrase(PhraseModel model) {
        delete(findIndexByPhrase(model.getPhrase()));
    }

    /**
     * deleting model from XML file
     * @param model - model for deleting from XML file
     */
    @Override
    public void deleteModel(PhraseModel model) {
        deletePhrase(model);
    }

    /**
     * close application action
     */
    @Override
    public void shutdown() {

    }

    /**
     * creating XML file
     */
    @Override
    public void create() {
        LOGGER.info("creating XML file for path: \"" + path + "\"");
        try {
            FileWriter fileWriter = new FileWriter(path, false);
            fileWriter.write(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLBEGIN));
            fileWriter.write(PhraseXmlAdapter.parseToXML((PhraseModel) null, XMLEND));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String findPhrase(String quote) {
        LOGGER.info("finding by phrase = \"" + quote + "\"");
        PhraseModel phrase;
        int index = 0;
        do {
            phrase = parser.parseFromXML(path, index);
            if (phrase != null) {
                if (phrase.getPhrase().equals(quote)) {
                    LOGGER.info("find = " + index);
                    return phrase.getType();
                }
            }
            index++;
        } while (phrase != null);
        LOGGER.info("not founded");
        return null;
    }

    @Override
    public LinkedList<PhraseModel> findType(String type) {
        LOGGER.info("finding by type = \"" + type + "\"");
        LinkedList<PhraseModel> phrases = new LinkedList<>();
        PhraseModel phrase;
        int index = 0;
        do {
            phrase = parser.parseFromXML(path, index);
            if (phrase != null) {
                if (phrase.getType().equals(type)) {
                    phrases.add(phrase);
                }
            }
            index++;
        } while (phrase != null);
        return phrases;
    }

    @Override
    public void refresh() {

    }
}
