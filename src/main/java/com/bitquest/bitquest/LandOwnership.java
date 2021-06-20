package com.bitquest.bitquest;

import redis.clients.jedis.Jedis;

public class LandOwnership {
    Jedis redis;
    public LandOwnership(Jedis _redis) {
        this.redis=_redis;
    }
}
