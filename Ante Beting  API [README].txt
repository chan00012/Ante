|-----------------------------------------------------------------------------------------------------------------------------------|
|															ANTE BETTING API 														|	
|-----------------------------------------------------------------------------------------------------------------------------------|
|	LOGIN API																														|
|	/Ante/api/login												-log in on system either admin/customer								|
|-----------------------------------------------------------------------------------------------------------------------------------|
|	ADMIN API																														|
|	POST - /Ante/api/admin/customer/create						-create admin/customer account										|
|	GET - /Ante/api/admin/customer/								-view all customer.													|
|	GET - /Ante/api/admin/customer/{query}						-view customer base on query(username or firstname or lastname)		|
|	POST - /Ante/api/admin/customer/adjust						-adjust player's balance											|
|	POST - /Ante/api/admin/event/create							-create an event													|	
|	GET - /Ante/api/admin/event/								-show all events													|
|	GET - /Ante/api/admin/event/{eventtype}						-show events base on event type										|
|	GET - /Ante/api/admin/event/result/{eventcode}				-show result of event												|
|	POST - /Ante/api/admin/event/result/specify					-specify winner of event											|
|	GET - Ante/api/admin/bet/									-show all bets														|
|	GET - /Ante/api/admin/bet/{eventcode}						-show bets base on event code										|
|	GET - /Ante/api/admin/bet/user/{username}					-show bets base on user												|
|------------------------------------------------------------------------------------------------------------------------------------
|	CUSTOMER API																													|
|	GET - /Ante/api/customer/sports								-view available sports												|
|	GET - /Ante/api/customer/event/{eventtype}					-view open events based on event type								|
|	GET - /Ante/api/customer/balance							-view customer balance												|
|	GET - /Ante/api/customer/bet/show							-show bet history													|
|	POST - /Ante/api/customer/bet								-bet on event														|
|-----------------------------------------------------------------------------------------------------------------------------------|
|		postman ante api template link - https://www.getpostman.com/collections/947584b2038545804f22								|
|-----------------------------------------------------------------------------------------------------------------------------------|