# A City Directories Listing

The code is located at https://github.com/owsega/CityDirectory

Features:
1. List cities in a RecyclerView
2. Allow filtering of the cities by prefix
3. When a city is clicked, a map is shown centered on the coordinates of the city.
4. Cities are loaded from a json built in the app
5. Search Strategy: At startup, data is processed into a ConcurrentSkipListMap, mapping city string representation (the city name concantenated with the country, e.g. El Ad, SO) in lowercase to the City object. A ConcurrentSkipListMap is a scalable map that is multithreading-safe and provides fast access to the elements (in log(n) time, on average).
6. Data presentation is done with PagedListAdapter provided in the SupportLibrary. Filtering is done in the ViewModel, and once a newly filtered map is ready, it is swapped into the view using LivePagedListBuilder.