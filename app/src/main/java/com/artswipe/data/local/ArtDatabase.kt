package com.artswipe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.artswipe.data.local.model.ArtworkEntity
import com.artswipe.data.local.model.SwipeRecordEntity

@Database(entities = [ArtworkEntity::class, SwipeRecordEntity::class], version = 2005, exportSchema = false)
abstract class ArtDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao
}
