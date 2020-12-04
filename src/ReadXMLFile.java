import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReadXMLFile {
    private final List<Item> items = new ArrayList<>();

    public static void main(String[] args) {
        ReadXMLFile readXMLFile = new ReadXMLFile();

        List<String> files = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            files.add("ebay-data/items-{#}.xml".replace("{#}", String.valueOf(i)));
        }

        readXMLFile.computeFiles(files);
    }

    public void computeFiles(List<String> files) {
        for (String file : files) {
            computeFile(file);
        }
        System.out.println(items.size());
    }

    private void computeFile(String file) {
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("Item");

            for (int itemIndex = 0; itemIndex < nodeList.getLength(); itemIndex++) {

                Node node = nodeList.item(itemIndex);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    addItem(mapItem(element));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Item mapItem(Element element) {
        Item item = new Item();
        item.id = getValueAsInteger(element, "ItemID");
        item.name = getValueAsString(element, "Name");
        item.currently = getValueAsDouble(element, "Currently");
        item.first_did = getValueAsDouble(element, "First_Bid");
        item.number_of_bids = getValueAsInteger(element, "Number_of_Bids");
        item.country = getValueAsString(element, "Country");
        item.started = getValueAsString(element, "Started");
        item.ends = getValueAsString(element, "Ends");
        item.description = getValueAsString(element, "Description");
        return item;
    }

    private void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    private String getValueAsString(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return value != null ? value : "";
    }

    private int getValueAsInteger(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return !Objects.equals(value, "") ? Integer.parseInt(value) : 0;
    }

    private double getValueAsDouble(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return (!Objects.equals(value, "")) ? Double.parseDouble(value.replace("$", "")) : 0;
    }

    private class Item {
        long id = 0L;
        String name = "";
        double currently;
        double first_did;
        int number_of_bids;
        String country = "";
        String started = "";
        String ends = "";
        String description = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return id == item.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Item{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", currently=" + currently +
                    ", first_did=" + first_did +
                    ", number_of_bids=" + number_of_bids +
                    ", country='" + country + '\'' +
                    ", started='" + started + '\'' +
                    ", ends='" + ends + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
