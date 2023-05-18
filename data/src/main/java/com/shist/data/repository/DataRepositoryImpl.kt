package com.shist.data.repository

import android.content.Context
import com.shist.data.model.BuildingItemJson
import com.shist.data.repository.mappers.AddressItemDBMapper
import com.shist.data.repository.mappers.BuildingItemDBMapper
import com.shist.data.repository.mappers.IconItemDBMapper
import com.shist.data.repository.mappers.StructuralObjectItemDBMapper
import com.shist.data.retrofit.MapDataApi
import com.shist.data.roomDB.BuildingItemsDatabase
import com.shist.data.roomDB.entities.buildingItem.BuildingItemDB
import com.shist.data.roomDB.entities.buildingItem.BuildingItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.adressItem.AddressItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.StructuralObjectItemJsonMapper
import com.shist.data.roomDB.entities.buildingItem.structuralObjectItem.iconItem.IconItemJsonMapper
import com.shist.domain.BuildingItem
import com.shist.domain.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This is implementation of repository based on its interface
class DataRepositoryImpl(private val buildingItemsDatabase: BuildingItemsDatabase,
                         private val service: MapDataApi,
                         private val buildingItemJsonMapper: BuildingItemJsonMapper,
                         private val buildingItemDBMapper: BuildingItemDBMapper,
                         private val structuralObjectItemJsonMapper: StructuralObjectItemJsonMapper,
                         private val structuralObjectItemDBMapper: StructuralObjectItemDBMapper,
                         private val addressItemJsonMapper: AddressItemJsonMapper,
                         private val addressItemDBMapper: AddressItemDBMapper,
                         private val iconItemJsonMapper: IconItemJsonMapper,
                         private val iconItemDBMapper: IconItemDBMapper,
                         private val context: Context
) : DataRepository {

    // This function checks if given item has ID or not
    private fun isItemWithID(itemJson: BuildingItemJson?): Boolean {
        return if (itemJson == null) {
            false
        } else {
            itemJson.id != null
        }
    }

    // This function checks if given item is empty or not
    private fun isItemNotEmpty(item: BuildingItemDB?): Boolean {
        return if (item == null)
            false
        else {
            item.buildingItemEntityDB.inventoryUsrreNumber != null ||
                    item.buildingItemEntityDB.name != null ||
                    item.buildingItemEntityDB.isModern != null ||
                    item.buildingItemEntityDB.type != null ||
                    item.buildingItemEntityDB.markerPath != null
        }
    }

    // This function is needed to get actual data from server
    override suspend fun loadData() {
        try {
            /*val jsonFile = File("./data.json")
            val jsonString = *//*jsonFile.readText()*//* context.assets.open("data.json").bufferedReader().use { it.readText() }
            val generatedList = Gson().fromJson(jsonString, Array<BuildingItemJson>::class.java).toList()*/
            val items = service.getAllBuildings()/*generatedList*/
                ?.filter { isItemWithID(it) } // Clean list from items with null id
                ?.map {
                    /*val itemsImages : List<BuildingItemImageJson> =
                        service.getImagesWithBuildingId(it.id!!)*//*emptyList()*/
                    BuildingItemJsonMapper().fromJsonToRoomDB(it/*, itemsImages*/)!!
                }
                ?.filter { isItemNotEmpty(it) } // Clean DB from items with empty data
            buildingItemsDatabase.buildingItemsDao().insertBuildingItemsList(items!!)
        } catch (e: Throwable) {
            throw NullPointerException("Error: " +
                    "Some BuildingItem (or even whole list) from json is empty!\n" + e.message)
        }
    }

    // This function return list of all items that are in local database
    override fun getAllBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
            "Some item is nullable!\n" + e.message)
        }
    }

    override fun getHistoricalBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! }.filter { buildingItem -> buildingItem.type == TYPE_HISTORICAL } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
                    "Some item is nullable!\n" + e.message)
        }
    }

    override fun getAdministrativeBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! }.filter { buildingItem -> buildingItem.type == TYPE_ADMINISTRATIVE } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
                    "Some item is nullable!\n" + e.message)
        }
    }

    override fun getEducationalBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! }.filter { buildingItem -> buildingItem.type == TYPE_EDUCATIONAL } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
                    "Some item is nullable!\n" + e.message)
        }
    }

    override fun getDormitoryBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! }.filter { buildingItem -> buildingItem.type == TYPE_DORMITORY } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
                    "Some item is nullable!\n" + e.message)
        }
    }

    override fun getMultifunctionalBuildings(): Flow<List<BuildingItem>> {
        try {
            return buildingItemsDatabase.buildingItemsDao().getAllBuildingItems().map { list ->
                list.map { BuildingItemDBMapper().fromDBToDomain(it)!! }.filter { buildingItem -> buildingItem.type == TYPE_MULTIFUNCTIONAL } }
        } catch (e: Throwable) {
            throw NullPointerException("Error while getting building items: " +
                    "Some item is nullable!\n" + e.message)
        }
    }

    companion object {
        private const val TYPE_HISTORICAL = "историческое"
        private const val TYPE_EDUCATIONAL = "учебное"
        private const val TYPE_ADMINISTRATIVE = "административное"
        private const val TYPE_DORMITORY = "общежитие"
        private const val TYPE_MULTIFUNCTIONAL = "многофункциональное"
    }

}