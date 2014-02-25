## Tick5 and the Tick5 Datatracker

The Tick5 project aims at finding and highlighting 5 tweets that, for a group of people with a common passion or a set of common attribute values, seem to be the most important tweets that have been active during the last 15 minutes.

Tick5 finds these tweets by tracking the twitter activity of a panel of 2000 people that share this common passion or these common attributes. It tracks all retweets and likes and uses a 'smart' algorithm that upgrades tweets that contain domain specific words or refer to domain specific websites. The algorithm downgrades tweets that contain words that point to publicity or spam or that refer to sites that have been tagged as spamsource.

The result of the Tick5 processing is the publication, every quarter, on the quarter, of a small and simple json array of 5 tweets + metadata that can be used on websites, apps or in other processes.

Possible domains of Tick5 processing are:

* Passion domains:
	* Technology
	* Science
	* Fashion&design
	* Finance
	* Media
	* Music

* Common attributes
	* Language
	* Location

The reference implementation of tick5 is running and tracking technology news. The results are available as ...

* [a service](http://dblnd.com/api/ticks/recent)
* [a website](http://dblnd.com/)
* [a mobile site](http://m.dblnd.com/)
* [a twitter stream](http://twitter.com/dblnd)
