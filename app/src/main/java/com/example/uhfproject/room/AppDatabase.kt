package com.example.uhfproject.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 定义数据库实体和版本
//@Database(
//    entities = [],
//    version = 1, // 数据库版本号，每次更新数据库结构时需要增加
//    exportSchema = false  // 导出架构用于版本历史跟踪
//)
//abstract class AppDatabase : RoomDatabase() {
//
//    // 定义DAO访问方法
////    abstract fun userDao(): UserDao
//    // 添加其他DAO...
//
//    companion object {
//        // 单例实例，使用Volatile确保线程安全
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        // 数据库名称
//        private const val DATABASE_NAME = "app_database.db"
//
//        // 获取数据库实例
//        fun getInstance(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    DATABASE_NAME
//                )
//                    .addCallback(DatabaseCallback()) // 添加数据库创建/打开回调
////                    .addMigrations(MIGRATION_1_2) // 添加数据库迁移策略
//                    .fallbackToDestructiveMigration() // 如果找不到迁移策略，则破坏性重建
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//
//        // 数据库创建回调
//        private class DatabaseCallback : RoomDatabase.Callback() {
//            override fun onCreate(db: SupportSQLiteDatabase) {
//                super.onCreate(db)
//                // 数据库首次创建时执行的操作
//                // 可以插入初始数据等
//            }
//
//            override fun onOpen(db: SupportSQLiteDatabase) {
//                super.onOpen(db)
//                // 数据库每次打开时执行的操作
//            }
//        }
//
//        // 数据库迁移策略示例（从版本1到版本2）
//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // 执行从版本1到版本2的迁移操作
//                // 例如添加新表或修改表结构
//                database.execSQL("ALTER TABLE user ADD COLUMN phone_number TEXT")
//            }
//        }
//
//        // 可以添加更多迁移策略
//        // private val MIGRATION_2_3 = object : Migration(2, 3) { ... }
//    }
//}