import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.*;

public class ReadXMLFile {
    private final List<Item> items = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<Bid> bids = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();
    private final Set<String> bidders = new HashSet<>();

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
        /*System.out.println("Items: " + items.size());
        System.out.println("Locations: " + locations.size());
        System.out.println("Users: " + users.size());
        System.out.println("Bids: " + bids.size());
        for (Bid bid : bids) {
            bidders.add(bid.user_id);
        }
        System.out.println("Bidders: " + bidders.size());
        int c = 0;
        for (Location location : locations) {
            if (location.latitude.length() > 0 | location.longitude.length() > 0) {
                c++;
            }
        }
        System.out.println(c);*/
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
                    Element itemElement = (Element) node;

                    Item item = mapToItem(itemElement);
                    if (!items.contains(item)) {
                        items.add(item);
                        isAddLocation(mapToLocation(itemElement, item.id));
                        addBids(itemElement, item.id);
                    }
                    isAddUser(mapToUser(itemElement));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Item mapToItem(Element ele) {
        Item item = new Item();
        item.id = getValueAsInteger(ele, "ItemID");
        item.name = ele.getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
        item.currently = getValueAsDouble(ele, "Currently");
        item.first_did = getValueAsDouble(ele.getElementsByTagName("First_Bid").item(0).getFirstChild().getNodeValue());
        item.number_of_bids = getValueAsInteger(ele.getElementsByTagName("Number_of_Bids").item(0).getFirstChild().getNodeValue());
        item.country = getValue(ele, "Country");
        item.started = ele.getElementsByTagName("Started").item(0).getFirstChild().getNodeValue();
        item.ends = ele.getElementsByTagName("Ends").item(0).getFirstChild().getNodeValue();
        item.description = getValue(ele, "Description");
        return item;
    }

    private Location mapToLocation(Element ele, long itemId) {
        Location location = new Location();
        Element locationEle = (Element) ele.getElementsByTagName("Location").item(0);
        location.item_id = itemId;
        location.latitude = locationEle.getAttribute("Latitude");
        location.longitude = locationEle.getAttribute("Longitude");
        location.place = locationEle.getFirstChild().getNodeValue();
        return location;
    }

    private User mapToUser(Element ele) {
        User user = new User();
        Element sellerEle = (Element) ele.getElementsByTagName("Seller").item(0);
        user.user_id = sellerEle.getAttribute("UserID");
        user.rating = sellerEle.getAttribute("Rating");
        user.country = ele.getElementsByTagName("Country").item(0).getFirstChild().getNodeValue();
        user.place = ele.getElementsByTagName("Location").item(0).getFirstChild().getNodeValue();
        return user;
    }

    private void addBids(Element ele, long itemId) {
        NodeList nodes = ((Element) ele.getElementsByTagName("Bids").item(0)).getElementsByTagName("Bid");
        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            Bid bid = new Bid();
            Element bidEle = (Element) nodes.item(nodeIndex);
            Element bidderEle = (Element) bidEle.getElementsByTagName("Bidder").item(0);
            bid.user_id = bidderEle.getAttribute("UserID");
            bid.item_id = itemId;
            bid.time = bidEle.getElementsByTagName("Time").item(0).getFirstChild().getNodeValue();
            bid.amount = getValueAsDouble(bidEle.getElementsByTagName("Amount").item(0).getFirstChild().getNodeValue());
            isAddBid(bid);
        }
    }

    private boolean isAddLocation(Location location) {
        if (!locations.contains(location)) {
            locations.add(location);
            return true;
        }
        return false;
    }

    private boolean isAddItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
            return true;
        }
        return false;
    }

    private boolean isAddBid(Bid bid) {
        if (!bids.contains(bid)) {
            bids.add(bid);
            return true;
        }
        return false;
    }

    private boolean isAddUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            return true;
        }
        return false;
    }

    private String getValue(Element ele, String att) {
        try {
            return ele.getElementsByTagName(att).item(0).getFirstChild().getNodeValue();
        } catch (Exception ex) {
            return "";
        }
    }

    private double getValueAsDouble(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return (!Objects.equals(value, "")) ? Double.parseDouble(value
                .replace("$", "")
                .replace(",", "")) : 0;
    }

    private int getValueAsInteger(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        return !Objects.equals(value, "") ? Integer.parseInt(value) : 0;
    }

    private double getValueAsDouble(String value) {
        return (!Objects.equals(value, "")) ? Double.parseDouble(value
                .replace("$", "")
                .replace(",", "")) : 0;
    }

    private int getValueAsInteger(String value) {
        return (!Objects.equals(value, "")) ? Integer.parseInt(value) : 0;
    }

    private class Location {
        long item_id;
        String place = "";
        String latitude;
        String longitude;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return item_id == location.item_id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(item_id);
        }
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
    }

    private class User {
        String user_id = "";
        String rating = "";
        String country = "";
        String place = "";

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(user_id, user.user_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user_id);
        }
    }

    private class Bid {
        String user_id = "";
        long item_id;
        String time = "";
        double amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bid bid = (Bid) o;
            return Objects.equals(user_id, bid.user_id) && Objects.equals(item_id, bid.item_id) && Objects.equals(time, bid.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user_id, item_id, time);
        }
    }

    private String asString(Node node) {
        StringWriter writer = new StringWriter();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            // @checkstyle MultipleStringLiterals (1 line)
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(OutputKeys.VERSION, "1.0");
            if (!(node instanceof Document)) {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            trans.transform(new DOMSource(node), new StreamResult(writer));
        } catch (final TransformerConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(ex);
        }
        return writer.toString();
    }

}
