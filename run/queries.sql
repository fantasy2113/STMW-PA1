# 1. Find the number of users in the database.
SELECT COUNT(*)
FROM users;

# 2. Find the number of items in "New York",
# i.e., itmes whose location is exactly the string "New York".
# Pay special attention to case sensitivity. E.g., you should not match items in "new york".
SELECT COUNT(*)
FROM ad.items_locations
WHERE place LIKE BINARY 'New York';

# 3. Find the number of auctions belonging to exactly four categories.
# Be careful to remove duplicates, if you store them.
SELECT COUNT(*)
FROM (SELECT item_id
      FROM ad.items_categories
      GROUP BY item_id
      HAVING COUNT(item_id) = 4) AS result;

# 4. Find the ID(s) of current (unsold) auction(s) with the highest bid.
# Remember that the data was captured at December 20th, 2001, one second after midnight.
# Pay special attention to the current auctions without any bid.
SELECT b.item_id, b.amount
FROM (SELECT ad.items.id FROM ad.items WHERE ad.items.ends >= '2001-12-20 00:00:01' AND ad.items.number_of_bids > 0) AS i,
     (SELECT * FROM ad.bids WHERE ad.bids.amount = (SELECT MAX(ad.bids.amount) FROM ad.bids)) AS b
WHERE i.id = b.item_id;

# 5. Find the number of sellers whose rating is higher than 1000.
SELECT COUNT(*)
FROM (SELECT b.id
      FROM ad.users AS b,
           ad.items AS i
      WHERE b.id = i.user_id
        AND b.rating > 1000
      GROUP BY b.id) AS result;

# 6. Find the number of users who are both sellers and bidders.
SELECT COUNT(*)
FROM (SELECT bs.id
      FROM ad.users AS bs,
           ad.items AS i,
           ad.bids AS b
      WHERE bs.id = i.user_id
        AND b.user_id = bs.id
      GROUP BY bs.id) AS result;

# 7. Find the number of categories that include at least one item with a bid of more than $100.
SELECT COUNT(*)
FROM (SELECT ic.category_id
      FROM ad.items_categories AS ic,
           ad.bids AS b
      WHERE b.amount > 100
        AND b.item_id = ic.item_id
      GROUP BY ic.category_id) AS result;

#The number of entries in the item-table is 19532.

#The number of entries in the table that associates items with categories is 90269.

#The number of sellers is 13129.
SELECT COUNT(*)
FROM (SELECT user_id FROM ad.items GROUP BY user_id) AS result;
#The number of bidders is 7010.
SELECT COUNT(*)
FROM (SELECT user_id FROM ad.bids GROUP BY user_id) AS result;

#The number of items that have a buyPrice is 1959.

#The number of items that have longitude/latitude information is 13090.
