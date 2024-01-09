package me.neon.mail.service.sql

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.annotations.Expose

class Exclude: ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean {
        return f.getAnnotation(Expose::class.java) != null
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
        return clazz.getAnnotation(Expose::class.java) != null
    }
}