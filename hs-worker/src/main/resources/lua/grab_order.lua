
local now=tonumber(ARGV[2])
local rateKey="grab:rate:"..ARGV[1]
redis.call('zremrangebyscore',rateKey,0,now-30000)
local count=redis.call('zcard',rateKey)
if count>=2 then
   return -2
end
local time=redis.call('time')
redis.call('zadd',rateKey,now,now..":"..time[2])

	local key=KEYS[1]
	local workerId=ARGV[1];

	local status=redis.call('get',key)
	if status==false then
	    return -1
	end
	if status~='0' then
	    return 0;
	else
	    redis.call('set',key,workerId)
	    return 1;
	end
