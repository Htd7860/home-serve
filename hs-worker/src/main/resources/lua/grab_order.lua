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
