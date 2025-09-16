🧩 Data Structure Choice

I chose to implement a B-Tree for the following reasons:
1. ``A very good choice for large data sets:``
- B-Trees are already widely used in databases and file systems, and since the focus of the internship is on big data, databases, and optimized queries, it was natural to choose B-Tree as the best option.
- B-Tree's main strength is that the keys are already sorted and the tree is automatically balanced.
2. ``Previous experience:``
- We already had a "key-value" engine faculty project, where we implemented a MemTable (and later N-Memtables) using B-Trees and SkipLists, although not thread-safe at that time.
- I wanted to revise my previous knowledge and upgrade what I already know. I first considered choosing RB trees, since I didn't have a chance to implement them on my own and I think they are very, very cool :), but in the end, I chose B-Tree for the reasons already mentioned above (1).
