# A City Directories Listing

The code is located at https://github.com/owsega/CityDirectory

Features:
1. List cities in a RecyclerView
2. Allow filtering of the cities by prefix
3. When a city is clicked, a map is shown centered on the coordinates of the city.
4. Cities are loaded from a json built in the app
5. Search Strategy. Data is stored in a trie. The trie is used to get the number of items matching a query text. The number of items (got from the trie) are then retrieved from a ConcurrentSkipMap, which is a sorted list of all the cities. 
6.