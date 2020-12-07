#1. Find the number of users in the database.
SELECT COUNT(*)
FROM bidders;

#2. Find the number of items in "New York",
#i.e., itmes whose location is exactly the string "New York".
#Pay special attention to case sensitivity. E.g., you should not match items in "new york".
SELECT COUNT(*)
FROM ad.items_locations
WHERE place LIKE BINARY 'New York';

#3. Find the number of auctions belonging to exactly four categories.
#Be careful to remove duplicates, if you store them.
SELECT COUNT(*)
FROM (SELECT item_id FROM ad.items_categories GROUP BY item_id HAVING COUNT(item_id) = 4) AS result;

#4. Find the ID(s) of current (unsold) auction(s) with the highest bid.
#Remember that the data was captured at December 20th, 2001, one second after midnight.
#Pay special attention to the current auctions without any bid.


#5. Find the number of sellers whose rating is higher than 1000.


#6. Find the number of users who are both sellers and bidders.


#7. Find the number of categories that include at least one item with a bid of more than $100.


#The number of entries in the item-table is 19532.

#The number of entries in the table that associates items with categories is 90269.

#The number of sellers is 13129.

#The number of bidders is 7010.

#The number of items that have a buyPrice is 1959.

#The number of items that have longitude/latitude information is 13090.
