"""
Cache Service - Redis-based caching for API responses
"""
import os
import json
import logging
from typing import Optional, Any
import redis

logger = logging.getLogger(__name__)


class CacheService:
    """Service for caching API responses in Redis"""

    def __init__(self):
        self.enabled = os.getenv("ENABLE_CACHE", "true").lower() == "true"

        if self.enabled:
            try:
                self.client = redis.Redis(
                    host=os.getenv("REDIS_HOST", "localhost"),
                    port=int(os.getenv("REDIS_PORT", 6379)),
                    db=int(os.getenv("REDIS_DB", 1)),
                    decode_responses=True
                )
                # Test connection
                self.client.ping()
                logger.info("Cache service initialized successfully")
            except Exception as e:
                logger.warning(f"Failed to connect to Redis: {str(e)}. Cache disabled.")
                self.enabled = False
                self.client = None
        else:
            self.client = None
            logger.info("Cache service disabled")

    def get(self, key: str) -> Optional[Any]:
        """
        Get value from cache

        Args:
            key: Cache key

        Returns:
            Cached value or None if not found
        """
        if not self.enabled:
            return None

        try:
            value = self.client.get(key)
            if value:
                logger.debug(f"Cache hit: {key}")
                return json.loads(value)
            logger.debug(f"Cache miss: {key}")
            return None
        except Exception as e:
            logger.error(f"Cache get error: {str(e)}")
            return None

    def set(self, key: str, value: Any, ttl: int = 300):
        """
        Set value in cache

        Args:
            key: Cache key
            value: Value to cache
            ttl: Time to live in seconds (default: 5 minutes)
        """
        if not self.enabled:
            return

        try:
            self.client.setex(
                key,
                ttl,
                json.dumps(value, default=str)
            )
            logger.debug(f"Cache set: {key} (TTL: {ttl}s)")
        except Exception as e:
            logger.error(f"Cache set error: {str(e)}")

    def delete(self, key: str):
        """Delete key from cache"""
        if not self.enabled:
            return

        try:
            self.client.delete(key)
            logger.debug(f"Cache delete: {key}")
        except Exception as e:
            logger.error(f"Cache delete error: {str(e)}")

    def clear_pattern(self, pattern: str):
        """Clear all keys matching pattern"""
        if not self.enabled:
            return

        try:
            keys = self.client.keys(pattern)
            if keys:
                self.client.delete(*keys)
                logger.info(f"Cleared {len(keys)} cache keys matching: {pattern}")
        except Exception as e:
            logger.error(f"Cache clear error: {str(e)}")
