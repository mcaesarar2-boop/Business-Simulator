package com.example.viewmodel

import com.example.data.*
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.SharedPreferences
import com.google.gson.Gson

import android.app.Application

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("tycoon_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // helper: return <NewCompanyCash, AmountForGlobal>
    fun processDecentralizedCashFlow(netProfit: Long, currentCompanyCash: Double): Pair<Double, Long> {
        if (netProfit > 0) {
            val internalRetained = netProfit * 0.6
            val dividendToGlobal = netProfit - internalRetained.toLong()
            return Pair(currentCompanyCash + internalRetained, dividendToGlobal)
        } else {
            val loss = -netProfit.toDouble()
            // Subtract loss entirely from company cash without taking anything from global/parent cash
            return Pair(currentCompanyCash - loss, 0L)
        }
    }

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private fun patchOwnedBusiness(business: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        val patchedBranches = (business.themeParkBranches ?: emptyList()).map { branch ->
            val ridesSafe = (branch.rides ?: emptyList()).map { ride ->
                ride.copy(
                    name = ride.name ?: "Wahana",
                    tierDescription = ride.tierDescription ?: "Wahana Kustom"
                )
            }.toMutableList()
            
            val facilitiesSafe = (branch.facilities ?: emptyList()).map { facility ->
                facility.copy(
                    id = facility.id ?: java.util.UUID.randomUUID().toString(),
                    zoneName = if (facility.zoneName.isNullOrBlank()) "Belum Terzonasi" else facility.zoneName,
                    imageUrl = facility.imageUrl
                )
            }
            
            branch.copy(
                facilities = facilitiesSafe,
                rides = ridesSafe,
                parkZones = branch.parkZones ?: mutableListOf(),
                priceRegular = if (branch.priceRegular == 0L) 15L else branch.priceRegular,
                priceTerusan = if (branch.priceTerusan == 0L) 35L else branch.priceTerusan,
                priceVIP = if (branch.priceVIP == 0L) 100L else branch.priceVIP,
                priceFamily = if (branch.priceFamily == 0L) 80L else branch.priceFamily,
                adBoostMultiplier = if (branch.adBoostMultiplier <= 0.0) 1.0 else branch.adBoostMultiplier
            )
        }
        val patchedSubs = (business.subsidiaries ?: emptyList()).map { patchOwnedBusiness(it) }
        return business.copy(
            studioType = business.studioType ?: "LIVE_ACTION",
            subsidiaries = patchedSubs,
            companyCash = business.companyCash ?: 0.0,
            themeParkBranches = patchedBranches
        )
    }

    private fun patchFoundations(list: List<com.example.data.FoundationEntity>?): List<com.example.data.FoundationEntity> {
        return try {
            (list ?: emptyList()).map { f ->
                val safeInstitutions = try {
                    (f.educationInstitutions ?: emptyList()).map { inst ->
                        val migratedTeachers = if (inst.teachers != null) {
                            var u = inst.teachers.umum ?: com.example.data.StaffRole()
                            var s = inst.teachers.spesialis ?: com.example.data.StaffRole()
                            var sn = inst.teachers.senior ?: com.example.data.StaffRole()
                            
                            if (u.customSalary == 0L) u = u.copy(customSalary = 3000L)
                            if (s.customSalary == 0L) s = s.copy(customSalary = 5000L)
                            if (sn.customSalary == 0L) sn = sn.copy(customSalary = 8000L)
                            
                            if (u.target == 0 && (u.active > 0 || u.recruiting > 0)) {
                                u = u.copy(target = u.active + u.recruiting)
                            }
                            if (s.target == 0 && (s.active > 0 || s.recruiting > 0)) {
                                s = s.copy(target = s.active + s.recruiting)
                            }
                            if (sn.target == 0 && (sn.active > 0 || sn.recruiting > 0)) {
                                sn = sn.copy(target = sn.active + sn.recruiting)
                            }
                            
                            @Suppress("DEPRECATION")
                            if (u.active == 0 && u.recruiting == 0 && (inst.teachers.umumCount ?: 0) > 0) {
                                u = u.copy(active = inst.teachers.umumCount ?: 0, target = inst.teachers.umumCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (s.active == 0 && s.recruiting == 0 && (inst.teachers.spesialisCount ?: 0) > 0) {
                                s = s.copy(active = inst.teachers.spesialisCount ?: 0, target = inst.teachers.spesialisCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (sn.active == 0 && sn.recruiting == 0 && (inst.teachers.seniorCount ?: 0) > 0) {
                                sn = sn.copy(active = inst.teachers.seniorCount ?: 0, target = inst.teachers.seniorCount ?: 0)
                            }
                            com.example.data.TeacherStaff(umum = u, spesialis = s, senior = sn)
                        } else {
                            com.example.data.TeacherStaff()
                        }

                        val migratedSupport = if (inst.supportStaff != null) {
                            var o = inst.supportStaff.ob ?: com.example.data.StaffRole()
                            var sat = inst.supportStaff.satpam ?: com.example.data.StaffRole()
                            var adm = inst.supportStaff.admin ?: com.example.data.StaffRole()
                            var ch = inst.supportStaff.chef ?: com.example.data.StaffRole()
                            
                            if (o.customSalary == 0L) o = o.copy(customSalary = 800L)
                            if (sat.customSalary == 0L) sat = sat.copy(customSalary = 1000L)
                            if (adm.customSalary == 0L) adm = adm.copy(customSalary = 1200L)
                            if (ch.customSalary == 0L) ch = ch.copy(customSalary = 2500L)
                            
                            if (o.target == 0 && (o.active > 0 || o.recruiting > 0)) {
                                o = o.copy(target = o.active + o.recruiting)
                            }
                            if (sat.target == 0 && (sat.active > 0 || sat.recruiting > 0)) {
                                sat = sat.copy(target = sat.active + sat.recruiting)
                            }
                            if (adm.target == 0 && (adm.active > 0 || adm.recruiting > 0)) {
                                adm = adm.copy(target = adm.active + adm.recruiting)
                            }
                            if (ch.target == 0 && (ch.active > 0 || ch.recruiting > 0)) {
                                ch = ch.copy(target = ch.active + ch.recruiting)
                            }
                            
                            @Suppress("DEPRECATION")
                            if (o.active == 0 && o.recruiting == 0 && (inst.supportStaff.janitorCount ?: 0) > 0) {
                                o = o.copy(active = inst.supportStaff.janitorCount ?: 0, target = inst.supportStaff.janitorCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (sat.active == 0 && sat.recruiting == 0 && (inst.supportStaff.securityCount ?: 0) > 0) {
                                sat = sat.copy(active = inst.supportStaff.securityCount ?: 0, target = inst.supportStaff.securityCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (adm.active == 0 && adm.recruiting == 0 && (inst.supportStaff.adminCount ?: 0) > 0) {
                                adm = adm.copy(active = inst.supportStaff.adminCount ?: 0, target = inst.supportStaff.adminCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (ch.active == 0 && ch.recruiting == 0 && (inst.supportStaff.chefCount ?: 0) > 0) {
                                ch = ch.copy(active = inst.supportStaff.chefCount ?: 0, target = inst.supportStaff.chefCount ?: 0)
                            }
                            com.example.data.SupportStaff(ob = o, satpam = sat, admin = adm, chef = ch)
                        } else {
                            com.example.data.SupportStaff()
                        }

                        com.example.data.EducationInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Institusi Lama",
                            level = inst.level ?: "SD",
                            curriculumType = inst.curriculumType ?: "Merdeka",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            monthlyOperationalCost = if (inst.monthlyOperationalCost < 0L) 100000L else inst.monthlyOperationalCost,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            monthlySpp = inst.monthlySpp,
                            currentStudents = inst.currentStudents,
                            buildingGrade = inst.buildingGrade ?: "Grade A",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            constructionMonthsTotal = inst.constructionMonthsTotal,
                            constructionMonthsLeft = inst.constructionMonthsLeft,
                            isOperational = inst.isOperational || (inst.constructionMonthsLeft == 0 && inst.currentStudents > 0),
                            teachers = migratedTeachers,
                            supportStaff = migratedSupport
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching educationInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                val safeHealthInstitutions = try {
                    (f.healthInstitutions ?: emptyList()).map { inst ->
                        val migratedMedical = if (inst.medicalStaff != null) {
                            var p = inst.medicalStaff.perawat ?: com.example.data.StaffRole()
                            var du = inst.medicalStaff.dokterUmum ?: com.example.data.StaffRole()
                            var ds = inst.medicalStaff.dokterSpesialis ?: com.example.data.StaffRole()
                            
                            if (p.customSalary == 0L) p = p.copy(customSalary = 4000L)
                            if (du.customSalary == 0L) du = du.copy(customSalary = 8000L)
                            if (ds.customSalary == 0L) ds = ds.copy(customSalary = 15000L)
                            
                            if (p.target == 0 && (p.active > 0 || p.recruiting > 0)) {
                                p = p.copy(target = p.active + p.recruiting)
                            }
                            if (du.target == 0 && (du.active > 0 || du.recruiting > 0)) {
                                du = du.copy(target = du.active + du.recruiting)
                            }
                            if (ds.target == 0 && (ds.active > 0 || ds.recruiting > 0)) {
                                ds = ds.copy(target = ds.active + ds.recruiting)
                            }
                            com.example.data.MedicalStaff(perawat = p, dokterUmum = du, dokterSpesialis = ds)
                        } else {
                            com.example.data.MedicalStaff()
                        }

                        val migratedSupport = if (inst.supportStaff != null) {
                            var o = inst.supportStaff.ob ?: com.example.data.StaffRole()
                            var sat = inst.supportStaff.satpam ?: com.example.data.StaffRole()
                            var adm = inst.supportStaff.admin ?: com.example.data.StaffRole()
                            var ch = inst.supportStaff.chef ?: com.example.data.StaffRole()
                            
                            if (o.customSalary == 0L) o = o.copy(customSalary = 800L)
                            if (sat.customSalary == 0L) sat = sat.copy(customSalary = 1000L)
                            if (adm.customSalary == 0L) adm = adm.copy(customSalary = 1200L)
                            if (ch.customSalary == 0L) ch = ch.copy(customSalary = 2500L)
                            
                            if (o.target == 0 && (o.active > 0 || o.recruiting > 0)) {
                                o = o.copy(target = o.active + o.recruiting)
                            }
                            if (sat.target == 0 && (sat.active > 0 || sat.recruiting > 0)) {
                                sat = sat.copy(target = sat.active + sat.recruiting)
                            }
                            if (adm.target == 0 && (adm.active > 0 || adm.recruiting > 0)) {
                                adm = adm.copy(target = adm.active + adm.recruiting)
                            }
                            if (ch.target == 0 && (ch.active > 0 || ch.recruiting > 0)) {
                                ch = ch.copy(target = ch.active + ch.recruiting)
                            }
                            com.example.data.SupportStaff(ob = o, satpam = sat, admin = adm, chef = ch)
                        } else {
                            com.example.data.SupportStaff()
                        }

                        com.example.data.HealthInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Klinik Lama",
                            level = inst.level ?: "Klinik",
                            serviceType = inst.serviceType ?: "Reguler",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            monthlyOperationalCost = if (inst.monthlyOperationalCost < 0L) 15000L else inst.monthlyOperationalCost,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            monthlyBillPerPatient = inst.monthlyBillPerPatient,
                            currentPatients = inst.currentPatients,
                            buildingGrade = inst.buildingGrade ?: "Grade A",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            constructionMonthsTotal = inst.constructionMonthsTotal,
                            constructionMonthsLeft = inst.constructionMonthsLeft,
                            isOperational = inst.isOperational || (inst.constructionMonthsLeft == 0 && inst.currentPatients > 0),
                            medicalStaff = migratedMedical,
                            supportStaff = migratedSupport
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching healthInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                val safeCharityInstitutions = try {
                    (f.charityInstitutions ?: emptyList()).map { inst ->
                        val migratedStaff = if (inst.charityStaff != null) {
                            var r = inst.charityStaff.relawan ?: com.example.data.StaffRole()
                            var ss = inst.charityStaff.staffSosial ?: com.example.data.StaffRole()
                            var ap = inst.charityStaff.ahliProgram ?: com.example.data.StaffRole()
                            
                            if (r.customSalary == 0L) r = r.copy(customSalary = 500L)
                            if (ss.customSalary == 0L) ss = ss.copy(customSalary = 3000L)
                            if (ap.customSalary == 0L) ap = ap.copy(customSalary = 7000L)
                            
                            if (r.target == 0 && (r.active > 0 || r.recruiting > 0)) {
                                r = r.copy(target = r.active + r.recruiting)
                            }
                            if (ss.target == 0 && (ss.active > 0 || ss.recruiting > 0)) {
                                ss = ss.copy(target = ss.active + ss.recruiting)
                            }
                            if (ap.target == 0 && (ap.active > 0 || ap.recruiting > 0)) {
                                ap = ap.copy(target = ap.active + ap.recruiting)
                            }
                            com.example.data.CharityStaff(relawan = r, staffSosial = ss, ahliProgram = ap)
                        } else {
                            com.example.data.CharityStaff()
                        }

                        com.example.data.CharityInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Badan Amal",
                            level = inst.level ?: "Humanitarian Aid",
                            scope = inst.scope ?: "Lokal",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            constructionTotalMonths = inst.constructionTotalMonths,
                            constructionLeftMonths = inst.constructionLeftMonths,
                            isOperational = inst.isOperational || (inst.constructionLeftMonths == 0 && inst.monthlyBeneficiaries > 0),
                            monthlyBeneficiaries = inst.monthlyBeneficiaries,
                            maxCapacity = if (inst.maxCapacity <= 0) com.example.data.calculateCharityMaxCapacity(inst.level ?: "Humanitarian Aid", inst.scope ?: "Lokal") else inst.maxCapacity,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            charityStaff = migratedStaff,
                            buildingGrade = inst.buildingGrade ?: "Grade A"
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching charityInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                com.example.data.FoundationEntity(
                    id = f.id ?: java.util.UUID.randomUUID().toString(),
                    name = f.name ?: "Yayasan Tanpa Nama",
                    type = f.type ?: com.example.data.FoundationType.EDUCATION,
                    isLegalized = f.isLegalized,
                    constructionMonthsLeft = f.constructionMonthsLeft,
                    endowmentFund = f.endowmentFund,
                    facilities = f.facilities ?: emptyList(),
                    educationInstitutions = safeInstitutions,
                    healthInstitutions = safeHealthInstitutions,
                    charityInstitutions = safeCharityInstitutions
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDebug", "Error patching foundations, fallback to empty: ${e.message}")
            emptyList()
        }
    }

    private val _monthProgress = kotlinx.coroutines.flow.MutableStateFlow(0f)
    private fun loadState(): PlayerState {
        val json = prefs.getString("player_state", null)
        if (json != null) {
            try {
                val state = gson.fromJson(json, PlayerState::class.java)
                
                // Auto-Migration saat Startup
                val patchedBusinesses = state.ownedBusinesses?.map { patchOwnedBusiness(it) } ?: emptyList()

                val patchedHoldings = state.holdingCompanies?.map { holding ->
                    val patchedSubs = holding.subsidiaries?.map { patchOwnedBusiness(it) } ?: emptyList()
                    holding.copy(subsidiaries = patchedSubs)
                } ?: emptyList()

                val migratedState = state.copy(
                    rebrandedCompanies = state.rebrandedCompanies ?: emptyMap(),
                    megaHolding = state.megaHolding ?: com.example.data.MegaHoldingState(),
                    ownedBusinesses = patchedBusinesses,
                    holdingCompanies = patchedHoldings,
                    allSubscriptions = if (state.allSubscriptions.isNullOrEmpty()) {
                        com.example.data.defaultLifestyleItems.map { defaultItem ->
                            val isActive = state.activeSubscriptions?.contains(defaultItem.name) == true
                            val isOwned = state.ownedGadgets?.contains(defaultItem.name) == true
                            defaultItem.copy(isActive = isActive, isOwned = isOwned)
                        }
                    } else {
                        val currentNames = state.allSubscriptions.map { it.name }.toSet()
                        val missingDefaults = com.example.data.defaultLifestyleItems.filterNot { currentNames.contains(it.name) }
                        state.allSubscriptions + missingDefaults
                    },
                    travelDestinations = if (state.travelDestinations.isNullOrEmpty()) {
                        com.example.data.defaultTravelDestinations
                    } else {
                        state.travelDestinations
                    },
                    totalTripsTaken = state.totalTripsTaken,
                    foundations = patchFoundations(state.foundations)
                )
                
                // Simpan pembaruan jika ini adalah migrasi sukses
                prefs.edit().putString("player_state", gson.toJson(migratedState)).apply()
                return migratedState
            } catch(e: Exception) { 
                e.printStackTrace()
                android.util.Log.e("AppDebug", "Init error parsing JSON, protecting save: ${e.message}")
                // Kritis: JANGAN hapus data save (prefs.edit().remove().apply()), lindungi data lama pemain!
                // Return default state but flag it so it won't be saved over
                return PlayerState().copy(cash = -1L) // Using cash = -1L as a flag for failed load
            }
        }
        return PlayerState()
    }

    fun exportSaveGame(): String {
        val savePayload = _playerState.value.copy(
            customMarketAssets = _realEstateMarket.value,
            customCollectionAssets = _collectionList.value,
            customHousingAssets = _housingList.value
        )
        return gson.toJson(savePayload)
    }

    fun importSaveGame(jsonString: String): Boolean {
        try {
            val importedState = gson.fromJson(jsonString, com.example.data.PlayerState::class.java)
            if (importedState != null && importedState.cash >= -1L) {
                val patchedBusinesses = importedState.ownedBusinesses?.map { patchOwnedBusiness(it) } ?: emptyList()
                val patchedHoldings = importedState.holdingCompanies?.map { holding ->
                    val patchedSubs = holding.subsidiaries?.map { patchOwnedBusiness(it) } ?: emptyList()
                    holding.copy(subsidiaries = patchedSubs)
                } ?: emptyList()
                val finalState = importedState.copy(
                    ownedBusinesses = patchedBusinesses,
                    holdingCompanies = patchedHoldings,
                    foundations = patchFoundations(importedState.foundations)
                )
                _playerState.value = finalState
                
                if (importedState.customMarketAssets != null) {
                    _realEstateMarket.value = importedState.customMarketAssets
                    saveProperties(importedState.customMarketAssets)
                }
                
                if (importedState.customCollectionAssets != null) {
                    val base = com.example.data.initialCollectionItems + com.example.data.initialVehicleItems
                    val baseIds = base.map { it.id }.toSet()
                    val merged = base.toMutableList()
                    for (item in importedState.customCollectionAssets) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.CollectionItem(
                                id = itemId,
                                categoryId = (item.categoryId as String?) ?: "",
                                name = (item.name as String?) ?: "",
                                description = (item.description as String?) ?: "",
                                basePrice = item.basePrice,
                                imageUrl = (item.imageUrl as String?) ?: "",
                                releaseYear = item.releaseYear,
                                type = (item.type as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    _collectionList.value = merged
                    saveCollections(merged)
                }
                
                if (importedState.customHousingAssets != null) {
                    val base = com.example.data.initialHousingItems
                    val baseIds = base.map { it.id }.toSet()
                    val merged = base.toMutableList()
                    for (item in importedState.customHousingAssets) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && !itemId.startsWith("hs_") && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.HousingItem(
                                id = itemId,
                                name = (item.name as String?) ?: "",
                                location = (item.location as String?) ?: "",
                                type = (item.type as String?) ?: "",
                                buyPrice = item.buyPrice,
                                rentPrice = item.rentPrice,
                                imageUrl = (item.imageUrl as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    _housingList.value = merged
                    saveHousing(merged)
                }
                
                autoResolveMissingHousing(importedState)
                
                prefs.edit().putString("player_state", gson.toJson(importedState.copy(lastSavedTimeMs = System.currentTimeMillis()))).apply()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("AppDebug", "Import Save failed: ${e.message}")
        }
        return false
    }

    private fun saveState(state: PlayerState) {
        if (state.cash == -1L) {
            android.util.Log.e("AppDebug", "Save blocked due to corrupted state.")
            return
        }
        val jsonOld = prefs.getString("player_state", null)
        if (jsonOld != null) {
            try {
                val oldState = gson.fromJson(jsonOld, PlayerState::class.java)
                if (oldState.netWorth > 10_000_000 && state.netWorth < 1_000_000 && state.lastMonthExpenses < 1_000_000) {
                    android.util.Log.e("AppDebug", "Save blocked due to suspicious drop in net worth: from ${oldState.netWorth} to ${state.netWorth}")
                    return
                }
            } catch (e: Exception) {
                // Ignore if old state parse fails 
            }
        }
        prefs.edit().putString("player_state", gson.toJson(state.copy(lastSavedTimeMs = System.currentTimeMillis()))).apply()
    }
    val monthProgress: kotlinx.coroutines.flow.StateFlow<Float> = _monthProgress.asStateFlow()

    private fun loadCustomProperties(): List<com.example.data.PropertyItem> {
        val json = prefs.getString("property_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.PropertyItem>>() {}.type
                val savedList: List<com.example.data.PropertyItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) return savedList
            } catch(e: Exception) { 
                android.util.Log.e("AppDebug", "Init error (loadCustomProperties): ${e.message}")
                prefs.edit().remove("property_list_state").apply()
            }
        }
        return com.example.data.initialRealEstateCatalog
    }

    private fun saveProperties(list: List<com.example.data.PropertyItem>) {
        prefs.edit().putString("property_list_state", gson.toJson(list)).apply()
    }

    private val _realEstateMarket = MutableStateFlow(com.example.data.initialRealEstateCatalog)
    val realEstateMarket: StateFlow<List<com.example.data.PropertyItem>> = _realEstateMarket.asStateFlow()

    private val _cryptoList = MutableStateFlow(com.example.data.initialCryptoList)
    val cryptoList: StateFlow<List<com.example.data.CryptoItem>> = _cryptoList.asStateFlow()

    private fun loadCustomCollections(): List<com.example.data.CollectionItem> {
        val baseList = com.example.data.initialCollectionItems + com.example.data.initialVehicleItems
        val json = prefs.getString("collection_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.CollectionItem>>() {}.type
                val savedList: List<com.example.data.CollectionItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.CollectionItem(
                                id = itemId,
                                categoryId = (item.categoryId as String?) ?: "",
                                name = (item.name as String?) ?: "",
                                description = (item.description as String?) ?: "",
                                basePrice = item.basePrice,
                                imageUrl = (item.imageUrl as String?) ?: "",
                                releaseYear = item.releaseYear,
                                type = (item.type as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    return merged
                }
            } catch(e: Exception) { 
                android.util.Log.e("AppDebug", "Init error (loadCustomCollections): ${e.message}")
                prefs.edit().remove("collection_list_state").apply()
            }
        }
        return baseList
    }
    
    private fun saveCollections(list: List<com.example.data.CollectionItem>) {
        prefs.edit().putString("collection_list_state", gson.toJson(list)).apply()
    }

    private val _collectionList = MutableStateFlow(com.example.data.initialCollectionItems + com.example.data.initialVehicleItems)
    val collectionList: StateFlow<List<com.example.data.CollectionItem>> = _collectionList.asStateFlow()

    private val _currentYearStartups = MutableStateFlow(com.example.data.generateYearlyStartups(1))
    val currentYearStartups: StateFlow<List<com.example.data.StartupInvestment>> = _currentYearStartups.asStateFlow()

    private val _preciousMetalsList = MutableStateFlow(com.example.data.initialPreciousMetals)
    val preciousMetalsList: StateFlow<List<com.example.data.PreciousMetal>> = _preciousMetalsList.asStateFlow()

    private fun loadCustomHousing(): List<com.example.data.HousingItem> {
        val baseList = com.example.data.initialHousingItems
        val json = prefs.getString("housing_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.HousingItem>>() {}.type
                val savedList: List<com.example.data.HousingItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && !itemId.startsWith("hs_") && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.HousingItem(
                                id = itemId,
                                name = (item.name as String?) ?: "",
                                location = (item.location as String?) ?: "",
                                type = (item.type as String?) ?: "",
                                buyPrice = item.buyPrice,
                                rentPrice = item.rentPrice,
                                imageUrl = item.imageUrl
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    return merged
                }
            } catch(e: Exception) { 
                prefs.edit().remove("housing_list_state").apply()
            }
        }
        return baseList
    }

    private fun saveHousing(list: List<com.example.data.HousingItem>) {
        prefs.edit().putString("housing_list_state", gson.toJson(list)).apply()
    }

    private fun autoResolveMissingHousing(state: PlayerState) {
        val currentHs = _housingList.value.toMutableList()
        val hsIds = currentHs.map { it.id }.toSet()
        val mutableHsIds = hsIds.toMutableSet()
        var addedAny = false
        
        for (owned in state.ownedHouses) {
            if (owned.housingId.isNotEmpty() && owned.housingId !in mutableHsIds) {
                val placeholder = com.example.data.HousingItem(
                    id = owned.housingId,
                    name = if (owned.housingId.startsWith("prop_custom_")) "Mansion Kustom" else "Hunian Kustom",
                    location = "Unknown",
                    type = "Custom Property",
                    buyPrice = owned.purchasedPrice,
                    rentPrice = (owned.purchasedPrice / 200).coerceAtLeast(1L),
                    imageUrl = owned.customImageUrl ?: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80"
                )
                currentHs.add(placeholder)
                mutableHsIds.add(owned.housingId)
                addedAny = true
            }
        }
        
        for (rented in state.rentedHouses) {
            if (rented.housingId.isNotEmpty() && rented.housingId !in mutableHsIds) {
                val placeholder = com.example.data.HousingItem(
                    id = rented.housingId,
                    name = if (rented.housingId.startsWith("prop_custom_")) "Mansion Sewa Kustom" else "Sewa Hunian Kustom",
                    location = "Unknown",
                    type = "Custom Property",
                    buyPrice = rented.monthlyRent * 200,
                    rentPrice = rented.monthlyRent,
                    imageUrl = rented.customImageUrl ?: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80"
                )
                currentHs.add(placeholder)
                mutableHsIds.add(rented.housingId)
                addedAny = true
            }
        }
        
        if (addedAny) {
            _housingList.value = currentHs
            saveHousing(currentHs)
        }
    }

    private val _housingList = MutableStateFlow(loadCustomHousing())
    val housingList: StateFlow<List<com.example.data.HousingItem>> = _housingList.asStateFlow()

    private val _tycoonList = MutableStateFlow(com.example.data.getInitialBillionaires())
    val tycoonList: StateFlow<List<com.example.data.Tycoon>> = _tycoonList.asStateFlow()



    // --- Advanced General & Mini Game Experiment Settings ---
    private val _monthDurationSeconds = MutableStateFlow(120f) // default 120 seconds for game-month
    val monthDurationSeconds: StateFlow<Float> = _monthDurationSeconds.asStateFlow()

    private val _stockIntervalSeconds = MutableStateFlow(30.0f) // default stock fluctuation interval
    val stockIntervalSeconds: StateFlow<Float> = _stockIntervalSeconds.asStateFlow()

    // Default SVG Path (Crown configuration)
    private val _companyLogoSvgPath = MutableStateFlow("M 10 90 L 10 30 L 35 60 L 50 20 L 65 60 L 90 30 L 90 90 Z")
    val companyLogoSvgPath: StateFlow<String> = _companyLogoSvgPath.asStateFlow()

    private val _companyLogoFillColorHex = MutableStateFlow("#FFD700") // gold
    val companyLogoFillColorHex: StateFlow<String> = _companyLogoFillColorHex.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isDarkModeSimulated = MutableStateFlow(true)
    val isDarkModeSimulated: StateFlow<Boolean> = _isDarkModeSimulated.asStateFlow()

    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _gameDifficulty = MutableStateFlow("Normal") // "Easy", "Normal", "Hard", "Elite Tycoon"
    val gameDifficulty: StateFlow<String> = _gameDifficulty.asStateFlow()

    private val _marketVolatilityFactor = MutableStateFlow(1.0f) // Volatility/Shift factor: 0.1x to 5.0x
    val marketVolatilityFactor: StateFlow<Float> = _marketVolatilityFactor.asStateFlow()

    fun updateMonthDuration(seconds: Float) {
        _monthDurationSeconds.value = seconds.coerceIn(10f, 7200f)
    }

    fun updateStockInterval(seconds: Float) {
        _stockIntervalSeconds.value = seconds.coerceIn(1.0f, 60.0f)
    }

    fun updateCompanyLogo(svgPath: String, colorHex: String) {
        _companyLogoSvgPath.value = svgPath
        _companyLogoFillColorHex.value = colorHex
    }

    private val _useShortNumberFormat = MutableStateFlow(false)
    val useShortNumberFormat: StateFlow<Boolean> = _useShortNumberFormat.asStateFlow()

    fun updateGameDifficultySettings(difficulty: String, volatility: Float) {
        _gameDifficulty.value = difficulty
        _marketVolatilityFactor.value = volatility.coerceIn(0.1f, 5.0f)
    }

    fun updateGeneralSettings(isNotification: Boolean, isDarkMode: Boolean, volume: Float, useShortNum: Boolean) {
        _isNotificationEnabled.value = isNotification
        _isDarkModeSimulated.value = isDarkMode
        _soundVolume.value = volume.coerceIn(0f, 1f)
        _useShortNumberFormat.value = useShortNum
    }
    
    fun injectCapitalToBusiness(instanceId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        return updateBusinessCash(currentState, instanceId, amount.toDouble(), amount, isDeposit = true)
    }

    fun withdrawCapitalFromBusiness(instanceId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        return updateBusinessCash(currentState, instanceId, amount.toDouble(), amount, isDeposit = false)
    }

    fun injectCapitalToHolding(holdingId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash < amount || amount <= 0) return false
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return false
        val updatedHoldings = currentState.holdingCompanies.map { 
            if (it.instanceId == holdingId) it.copy(holdingCash = it.holdingCash + amount) else it 
        }
        _playerState.value = currentState.copy(cash = currentState.cash - amount, holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
        return true
    }

    fun withdrawCapitalFromHolding(holdingId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return false
        if (holding.holdingCash < amount) return false
        val updatedHoldings = currentState.holdingCompanies.map { 
            if (it.instanceId == holdingId) it.copy(holdingCash = it.holdingCash - amount) else it 
        }
        _playerState.value = currentState.copy(cash = currentState.cash + amount, holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
        return true
    }

    fun purchaseThemeParkLand(businessInstanceId: String, landType: ThemeParkLandType) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val newBidding = com.example.data.ActiveBidding(landType = landType)
            val newBiddings = owned.activeThemeParkBiddings + newBidding
            val newOwned = owned.copy(activeThemeParkBiddings = newBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun submitThemeParkBiddingOffer(businessInstanceId: String, biddingId: String, offer: Long) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBiddings = owned.activeThemeParkBiddings.map { bidding ->
                if (bidding.id == biddingId) {
                    bidding.copy(
                        phase = com.example.data.BiddingPhase.WAITING_REPLY,
                        monthsLeft = 2,
                        playerOffer = offer
                    )
                } else bidding
            }
            val newOwned = owned.copy(activeThemeParkBiddings = updatedBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun cancelThemeParkBidding(businessInstanceId: String, biddingId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBiddings = owned.activeThemeParkBiddings.filter { it.id != biddingId }
            val newOwned = owned.copy(activeThemeParkBiddings = updatedBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun resolveThemeParkBiddingDeal(businessInstanceId: String, biddingId: String): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val bidding = owned.activeThemeParkBiddings.find { it.id == biddingId } ?: return false
            val cost = bidding.currentAskingPrice // Deal price is current asking price
            if (owned.companyCash >= cost) {
                val newBranch = com.example.data.ThemeParkBranch(
                    locationName = bidding.landType.locationName,
                    landType = bidding.landType,
                    remainingBiddingMonths = 0 // Wait, what if remainingBiddingMonths was used for construction? The prompt says "BUAT ThemeParkBranch baru dengan status lahan ini, lalu hapus objek ActiveBidding ini dari list." Since bidding is already done, let's set it to 0 so it's directly operational / constructable.
                )
                val updatedBranches = owned.themeParkBranches + newBranch
                val updatedBiddings = owned.activeThemeParkBiddings.filter { it.id != biddingId }
                val newOwned = owned.copy(
                    companyCash = owned.companyCash - cost,
                    themeParkBranches = updatedBranches,
                    activeThemeParkBiddings = updatedBiddings
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
                return true
            }
        }
        return false
    }

    fun buildThemeParkRide(
        businessInstanceId: String,
        branchId: String,
        rideTier: RideTier,
        customRideName: String,
        imageUrl: String? = null,
        zoneName: String? = null,
        ipThemeTitle: String? = null,
        ipThemeScore: Int? = null
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= rideTier.cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newRide = ThemeParkRide(
                        name = customRideName,
                        constructionMonthsLeft = rideTier.buildMonths,
                        isConstructing = true,
                        tierDescription = rideTier.description,
                        cost = rideTier.cost,
                        imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl,
                        zoneName = zoneName,
                        ipThemeTitle = ipThemeTitle,
                        ipThemeScore = ipThemeScore
                    )
                    branch.copy(rides = (branch.rides + newRide).toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - rideTier.cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun purchaseAdPackage(
        businessInstanceId: String,
        branchId: String,
        adName: String,
        durationMonths: Int,
        boostMultiplier: Double,
        cost: Long
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(
                        activeAdName = adName,
                        adMonthsLeft = durationMonths,
                        adBoostMultiplier = boostMultiplier
                    )
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun buildThemeParkFacility(
        businessInstanceId: String,
        branchId: String,
        catalogEntry: com.example.viewmodel.ThemeParkEngine.ThemeParkFacilityCatalogEntry,
        customZoneName: String = "Belum Terzonasi"
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= catalogEntry.buildCost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newFacility = ThemeParkFacility(
                        catalogId = catalogEntry.catalogId,
                        name = catalogEntry.name,
                        buildCost = catalogEntry.buildCost,
                        maintenanceCost = catalogEntry.maintenanceCost,
                        fnbBoostPercent = catalogEntry.fnbBoostPercent,
                        appealBoost = catalogEntry.appealBoost,
                        zoneName = customZoneName
                    )
                    branch.copy(facilities = branch.facilities + newFacility)
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - catalogEntry.buildCost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun updateThemeParkFacilityDetails(
        businessInstanceId: String,
        branchId: String,
        facilityId: String,
        newName: String,
        newZoneName: String,
        newImageUrl: String? = null
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedFac = branch.facilities.map { fac ->
                        if (fac.id == facilityId) {
                            fac.copy(name = newName, zoneName = newZoneName, imageUrl = newImageUrl)
                        } else fac
                    }
                    branch.copy(facilities = updatedFac)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun demolishThemeParkFacility(
        businessInstanceId: String,
        branchId: String,
        facilityId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(facilities = branch.facilities.filter { it.id != facilityId })
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun toggleThemeParkRidePause(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(isPaused = !ride.isPaused)
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun demolishThemeParkRide(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.filter { it.id != rideId }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun startThemeParkRideMaintenance(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val branch = owned.themeParkBranches.find { it.id == branchId } ?: return false
            val ride = branch.rides.find { it.id == rideId } ?: return false
            
            val tier = RideTier.values().find { it.cost == ride.cost }?.level ?: 1
            val duration = (tier / 2).coerceAtLeast(1)
            val costToPay = ride.maintenanceCost * duration
            
            if (owned.companyCash >= costToPay) {
                val updatedBranches = owned.themeParkBranches.map { b ->
                    if (b.id == branchId) {
                        val updatedRides = b.rides.map { r ->
                            if (r.id == rideId) {
                                r.copy(
                                    isUnderMaintenance = true,
                                    maintenanceMonthsLeft = duration,
                                    isPaused = false
                                )
                            } else r
                        }
                        b.copy(rides = updatedRides.toMutableList())
                    } else b
                }
                
                val newOwned = owned.copy(
                    companyCash = owned.companyCash - costToPay,
                    themeParkBranches = updatedBranches
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
                return true
            }
        }
        return false
    }

    fun deleteThemeParkBranch(businessInstanceId: String, branchId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val branchToRemove = owned.themeParkBranches.find { it.id == branchId }
            if (branchToRemove != null) {
                val landRefund = (branchToRemove.landType.basePrice * 0.5).toLong()
                val updatedBranches = owned.themeParkBranches.filter { it.id != branchId }
                val newOwned = owned.copy(
                    themeParkBranches = updatedBranches,
                    companyCash = owned.companyCash + landRefund
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
            }
        }
    }

    fun updateThemeParkBranchImage(businessInstanceId: String, branchId: String, newImageUrl: String?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(imageUrl = if (newImageUrl.isNullOrBlank()) null else newImageUrl)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun moveThemeParkZone(businessInstanceId: String, branchId: String, index: Int, isUp: Boolean) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newZones = branch.parkZones.toMutableList()
                    if (isUp && index > 0) {
                        val temp = newZones[index]
                        newZones[index] = newZones[index - 1]
                        newZones[index - 1] = temp
                    } else if (!isUp && index < newZones.size - 1) {
                        val temp = newZones[index]
                        newZones[index] = newZones[index + 1]
                        newZones[index + 1] = temp
                    }
                    branch.copy(parkZones = newZones)
                } else branch
            }
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun addThemeParkZone(businessInstanceId: String, branchId: String, zoneName: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && zoneName.isNotBlank()) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId && !branch.parkZones.contains(zoneName)) {
                    val newZones = branch.parkZones.toMutableList()
                    newZones.add(zoneName)
                    branch.copy(parkZones = newZones)
                } else branch
            }
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun updateThemeParkRideZoneAndIP(businessInstanceId: String, branchId: String, rideId: String, newZoneName: String?, newIpTitle: String?, newIpScore: Int?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(
                                zoneName = newZoneName,
                                ipThemeTitle = newIpTitle,
                                ipThemeScore = newIpScore
                            )
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun updateThemeParkRideDetails(businessInstanceId: String, branchId: String, rideId: String, newName: String, newImageUrl: String?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(
                                name = newName,
                                imageUrl = if (newImageUrl.isNullOrBlank()) null else newImageUrl
                            )
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun launchThemeParkBranch(businessInstanceId: String, branchId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(isLaunched = true)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun activateThemeParkHype(businessInstanceId: String, branchId: String, cost: Long): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(hasHypeMarketing = true, hypeMonthsLeft = 1)
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun updateThemeParkTicketPrices(businessInstanceId: String, branchId: String, pRegular: Long, pTerusan: Long, pVIP: Long, pFamily: Long) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            currentState.holdingCompanies.forEach { h ->
                val sub = h.subsidiaries.find { it.instanceId == businessInstanceId }
                if (sub != null) {
                    owned = sub
                    isNested = true
                    holdingId = h.instanceId
                }
            }
        }
        if (owned != null) {
            val updatedBranches = owned!!.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(
                        priceRegular = pRegular,
                        priceTerusan = pTerusan,
                        priceVIP = pVIP,
                        priceFamily = pFamily
                    )
                } else branch
            }
            val newOwned = owned!!.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun renameThemeParkBranch(businessInstanceId: String, branchId: String, newName: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(customName = newName)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    private fun updateBusinessCash(currentState: PlayerState, instanceId: String, businessCashChange: Double, playerCashChange: Long, isDeposit: Boolean): Boolean {
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        if (owned == null) return false
        
        if (isDeposit) {
            if (isNested && holdingId != null) {
                val holding = currentState.holdingCompanies.find { it.instanceId == holdingId }!!
                if (holding.holdingCash < playerCashChange) return false
            } else {
                if (currentState.cash < playerCashChange) return false
            }
            if (owned.companyCash + businessCashChange < 0) return false // Should not happen
        } else {
            if (owned.companyCash - businessCashChange < 0) return false
        }
        
        val actualBusinessChange = if (isDeposit) businessCashChange else -businessCashChange
        val newOwned = owned.copy(companyCash = owned.companyCash + actualBusinessChange)
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    val newHoldingCash = if (isDeposit) holding.holdingCash - playerCashChange else holding.holdingCash + playerCashChange
                    holding.copy(subsidiaries = newSubs, holdingCash = newHoldingCash)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            val newGlobalCash = if (isDeposit) currentState.cash - playerCashChange else currentState.cash + playerCashChange
            _playerState.value = currentState.copy(
                cash = newGlobalCash,
                ownedBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            )
        }
        saveState(_playerState.value)
        return true
    }

    fun setPlayerCash(amount: Long) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            cash = amount,
            netWorth = amount + (currentState.netWorth - currentState.cash)
        )
        saveState(_playerState.value)
    }

    fun investInStartup(startupId: String) {
        val startup = _currentYearStartups.value.find { it.id == startupId } ?: return
        val currentCash = _playerState.value.cash
        
        if (currentCash >= startup.requiredInvestment) {
            val newCash = currentCash - startup.requiredInvestment
            val activeList = _playerState.value.activeStartupInvestments.toMutableList()
            
            val activeInv = com.example.data.ActiveStartupInvestment(
                id = java.util.UUID.randomUUID().toString(),
                startupName = startup.name,
                investedAmount = startup.requiredInvestment,
                potentialReturn = (startup.requiredInvestment * startup.potentialReturnMultiplier).toLong(),
                monthsRemaining = startup.durationMonths,
                successProbability = startup.successProbability
            )
            activeList.add(activeInv)
            
            // Remove from available startups for this year
            val updatedStartups = _currentYearStartups.value.filter { it.id != startupId }
            _currentYearStartups.value = updatedStartups
            
            _playerState.value = _playerState.value.copy(
                cash = newCash,
                activeStartupInvestments = activeList
            )
        }
    }

    fun buyCrypto(symbol: String, price: Double, amount: Double) {
        val requiredCashUsd = price * amount
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val newCash = currentCash - requiredCashUsd.toLong()
            val ownedList = _playerState.value.ownedCrypto.toMutableList()
            
            val existing = ownedList.find { it.symbol == symbol }
            if (existing != null) {
                val totalAmount = existing.amount + amount
                val newAveragePrice = ((existing.amount * existing.averagePrice) + (amount * price)) / totalAmount
                ownedList.remove(existing)
                ownedList.add(com.example.data.OwnedCrypto(symbol, newAveragePrice, totalAmount))
            } else {
                ownedList.add(com.example.data.OwnedCrypto(symbol, price, amount))
            }
            
            _playerState.value = _playerState.value.copy(
                cash = newCash,
                ownedCrypto = ownedList
            )
        }
    }

    fun buyStock(ticker: String, price: Double, quantity: Long) {
        val stockToBuy = _stockList.value.find { it.ticker == ticker } ?: return
        val isIndo = ticker.contains(".JK")
        val requiredCashUsd = price * quantity
        val priceInUsd = price
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val currentState = _playerState.value
            val existingStocks = currentState.ownedStocks.toMutableList()
            val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }
            
            if (existingIndex != -1) {
                val existing = existingStocks[existingIndex]
                val newShares = existing.shares + quantity
                if (newShares < 0L) return // anti-overflow
                val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * priceInUsd)) / newShares
                existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
            } else {
                existingStocks.add(OwnedStock(ticker, priceInUsd, quantity))
            }

            _playerState.value = currentState.copy(
                cash = currentCash - requiredCashUsd.toLong(),
                ownedStocks = existingStocks,
                corporateStockPortfolio = existingStocks
            )
        }
    }

    fun sellStock(ticker: String, price: Double, quantity: Long) {
        val stockToSell = _stockList.value.find { it.ticker == ticker } ?: return
        val isIndo = ticker.contains(".JK")
        val revenueUsd = price * quantity

        val currentState = _playerState.value
        val existingStocks = currentState.ownedStocks.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex != -1) {
            val existing = existingStocks[existingIndex]
            if (existing.shares >= quantity) {
                val newShares = existing.shares - quantity
                if (newShares == 0L) {
                    existingStocks.removeAt(existingIndex)
                } else {
                    existingStocks[existingIndex] = existing.copy(shares = newShares)
                }
                
                _playerState.value = currentState.copy(
                    cash = currentState.cash + revenueUsd.toLong(),
                    ownedStocks = existingStocks,
                    corporateStockPortfolio = existingStocks
                )
            }
        }
    }

    private fun logToPrivateLedger(state: PlayerState, title: String, amount: Long, isIncome: Boolean): PlayerState {
        if (amount <= 0L) return state
        val record = com.example.data.PrivateLedgerRecord(
            monthTick = state.inGameMonth,
            title = title,
            amount = Math.abs(amount),
            isIncome = isIncome
        )
        val newList = (listOf(record) + state.privateLedgerHistory).take(200)
        return state.copy(privateLedgerHistory = newList)
    }

    fun buyPrivateStock(ticker: String, price: Double, quantity: Long) {
        val stockToBuy = _stockList.value.find { it.ticker == ticker } ?: return
        val requiredCashUsd = price * quantity
        val currentPrivateBalance = _playerState.value.privateBalance

        if (currentPrivateBalance >= requiredCashUsd) {
            val currentState = _playerState.value
            val existingStocks = currentState.privateStockPortfolio.toMutableList()
            val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }
            
            if (existingIndex != -1) {
                val existing = existingStocks[existingIndex]
                val newShares = existing.shares + quantity
                if (newShares < 0L) return
                val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * price)) / newShares
                existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
            } else {
                existingStocks.add(OwnedStock(ticker, price, quantity))
            }

            val nextState = currentState.copy(
                privateBalance = currentPrivateBalance - requiredCashUsd.toLong(),
                privateStockPortfolio = existingStocks
            )
            val loggedState = logToPrivateLedger(nextState, "Beli Saham $ticker ($quantity Lembar)", requiredCashUsd.toLong(), false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun sellPrivateStock(ticker: String, price: Double, quantity: Long) {
        val stockToSell = _stockList.value.find { it.ticker == ticker } ?: return
        val revenueUsd = price * quantity
        val currentState = _playerState.value
        val existingStocks = currentState.privateStockPortfolio.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex != -1) {
            val existing = existingStocks[existingIndex]
            if (existing.shares >= quantity) {
                val newShares = existing.shares - quantity
                if (newShares == 0L) {
                    existingStocks.removeAt(existingIndex)
                } else {
                    existingStocks[existingIndex] = existing.copy(shares = newShares)
                }
                
                val nextState = currentState.copy(
                    privateBalance = currentState.privateBalance + revenueUsd.toLong(),
                    privateStockPortfolio = existingStocks
                )
                val loggedState = logToPrivateLedger(nextState, "Jual Saham $ticker ($quantity Lembar)", revenueUsd.toLong(), true)
                _playerState.value = loggedState
                saveState(loggedState)
            }
        }
    }
    
    fun getSortedStocks(list: List<StockItem>, activeFilter: String): List<StockItem> {
        return when (activeFilter) {
            "Highest Dividend" -> list.sortedByDescending { getMarketStats(it).dividendYield }
            "Lowest Dividend" -> list.sortedBy { getMarketStats(it).dividendYield }
            "Highest Market Cap" -> list.sortedByDescending { it.sharesOutstanding * it.currentPrice }
            "Lowest Market Cap" -> list.sortedBy { it.sharesOutstanding * it.currentPrice }
            else -> list
        }
    }

    fun resetGameProgress() {
        _monthProgress.value = 0f
        _playerState.value = PlayerState(
            cash = 0,
            netWorth = 0,
            inGameMonth = 1,
            inGameYear = 1,
            lastMonthIncome = 0,
            lastMonthExpenses = 0,
            lastMonthNetProfit = 0,
            ownedBusinesses = emptyList(),
            ownedStocks = emptyList(),
            ownedProperties = emptyList(),
            ownedCrypto = emptyList(),
            activeStartupInvestments = emptyList(),
            ownedCollections = emptyList(),
            ownedMetals = emptyMap(),
            ownedHouses = emptyList(),
            rentedHouses = emptyList(),
            customBusinessCatalog = emptyList(),
            rebrandedCompanies = emptyMap()
        )
        saveState(_playerState.value)
    }

    fun rebrandCompany(ticker: String, oldName: String, newName: String) {
        val currentState = _playerState.value
        val updatedRebrands = currentState.rebrandedCompanies.toMutableMap()
        updatedRebrands[ticker] = newName
        
        _playerState.value = currentState.copy(rebrandedCompanies = updatedRebrands)
        saveState(_playerState.value)
        
        val newsText = "\uD83D\uDEA8 MEGA AKUISISI: $oldName telah resmi diakuisisi secara penuh dan kini berganti nama menjadi $newName! Pasar merespon dengan takjub."
        val newsItem = MarketNews(
            id = "rebrand_${System.currentTimeMillis()}",
            text = newsText,
            type = "BULL"
        )
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    fun integrateStockToHolding(ticker: String, newName: String) {
        val currentState = _playerState.value
        val ownedStocks = currentState.ownedStocks.toMutableList()
        val stockIndex = ownedStocks.indexOfFirst { it.ticker == ticker && !it.isIntegratedToHolding }
        if (stockIndex == -1) return
        
        ownedStocks[stockIndex] = ownedStocks[stockIndex].copy(isIntegratedToHolding = true)
        
        val liveStock = _stockList.value.find { it.ticker == ticker } ?: return
        
        val fallbackCatalogId = "corporate_hq" 
        val newBusiness = com.example.data.OwnedBusiness(
            instanceId = java.util.UUID.randomUUID().toString(),
            catalogId = fallbackCatalogId, 
            customName = newName,
            level = 1,
            acquiredStockTicker = ticker,
            parentId = null
        )
        
        var addedToHolding = false
        var updatedHoldings = currentState.holdingCompanies
        if (currentState.megaHolding.isActive && newBusiness.acquiredStockTicker == null) {
            val hQ = currentState.holdingCompanies.firstOrNull() 
            if (hQ != null) {
                updatedHoldings = currentState.holdingCompanies.map { h ->
                    if (h.instanceId == hQ.instanceId) {
                        h.copy(subsidiaries = h.subsidiaries + newBusiness)
                    } else h
                }
                addedToHolding = true
            }
        }
        
        if (addedToHolding) {
            _playerState.value = currentState.copy(
                ownedStocks = ownedStocks,
                corporateStockPortfolio = ownedStocks,
                holdingCompanies = updatedHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedStocks = ownedStocks,
                corporateStockPortfolio = ownedStocks,
                ownedBusinesses = currentState.ownedBusinesses + newBusiness
            )
        }
        
        saveState(_playerState.value)
        
        val newsText = "\uD83C\uDFE2 CORPORATE MERGER: Perusahaan Publik $ticker telah dicabut dari publik (Go-Private) dan diintegrasikan secara penuh ke dalam Mega Holding sebagai $newName!"
        val mergerNewsItem = MarketNews(
            id = "merger_${System.currentTimeMillis()}",
            text = newsText,
            type = "BULL"
        )
        _newsFeed.value = (listOf(mergerNewsItem) + _newsFeed.value).take(20)
    }

    private val _stockList = MutableStateFlow<List<StockItem>>(emptyList())
    val stockList: StateFlow<List<StockItem>> = _stockList.asStateFlow()

    private val initialPrices = mutableMapOf<String, Double>()

    private fun startCryptoMarketLoop() {
        viewModelScope.launch {
            val initialCryptoPrices = com.example.data.initialCryptoList.associate { it.symbol to it.currentPrice }
            while (true) {
                delay((_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L))
                val volatility = _marketVolatilityFactor.value * 2.5f // Crypto is more volatile
                val triggerNews = Math.random() < 0.10
                
                val updatedCrypto = _cryptoList.value.map { crypto ->
                    val baseline = initialCryptoPrices[crypto.symbol] ?: crypto.currentPrice
                    val changePct = (Math.random() - 0.5) * 0.015 * volatility
                    val newPrice = Math.max(0.000001, crypto.currentPrice * (1 + changePct))
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    
                    crypto.copy(
                        currentPrice = newPrice,
                        changePercentage = newChangePct
                    )
                }.toMutableList()

                if (triggerNews) {
                    val rand = Math.random()
                    val newsItem = when {
                        rand < 0.5 -> {
                            val boost = 0.03 + (Math.random() * 0.05)
                            updatedCrypto.replaceAll { c -> 
                                val baseline = initialCryptoPrices[c.symbol] ?: c.currentPrice
                                val newP = c.currentPrice * (1 + boost)
                                c.copy(currentPrice = newP, changePercentage = ((newP - baseline) / baseline) * 100)
                            }
                            MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO PUMP: Institusi besar mulai adopsi masal blockchain!", type = "BULL")
                        }
                        else -> {
                            val drop = -0.03 - (Math.random() * 0.05)
                            updatedCrypto.replaceAll { c -> 
                                val baseline = initialCryptoPrices[c.symbol] ?: c.currentPrice
                                val newP = c.currentPrice * (1 + drop)
                                c.copy(currentPrice = newP, changePercentage = ((newP - baseline) / baseline) * 100)
                            }
                            MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO CRASH: Regulasi ketat memukul pasar kripto!", type = "BEAR")
                        }
                    }
                    val newFeeds = listOf(newsItem) + _newsFeed.value
                    _newsFeed.value = newFeeds.take(20)
                }
                
                _cryptoList.value = updatedCrypto
            }
        }
    }

    private fun startTycoonMarketLoop() {
        viewModelScope.launch {
            while (true) {
                delay((_stockIntervalSeconds.value * 2000f).toLong().coerceAtLeast(200L))
                updateTycoons()
            }
        }
    }
    
    private fun updateTycoons() {
        val currentTycoons = _tycoonList.value
        val player = _playerState.value
        
        // Remove old player dummy entry if exists
        var updated = currentTycoons.filter { !it.isPlayer }.toMutableList()
        
        // add player
        updated.add(com.example.data.Tycoon("player", "You", player.netWorth, true))
        
        updated = updated.map { tycoon ->
            if (tycoon.isPlayer) {
                tycoon
            } else {
                val fluctuation = (Math.random() * 0.03 - 0.01) // -1% to +2%
                val newWorth = tycoon.netWorth + (tycoon.netWorth * fluctuation).toLong()
                tycoon.copy(netWorth = newWorth)
            }
        }.toMutableList()
        
        _tycoonList.value = updated.sortedByDescending { it.netWorth }
    }
    
    val earningsReport: StateFlow<EarningsReport> = kotlinx.coroutines.flow.combine(
        _playerState, _stockList, _realEstateMarket, _cryptoList
    ) { state, stockList, realEstateMarket, cryptoList ->
        var business = 0L
        state.ownedBusinesses.forEach { owned ->
            val catalogItem = getCatalogItem(owned.catalogId, state)
            if (catalogItem != null) {
                val (rev, _) = getBusinessStats(owned, catalogItem, state)
                business += rev
            }
        }
        state.holdingCompanies.forEach { holding ->
            business += com.example.data.CorporateFinanceManager.calculateHoldingMonthlyRevenue(holding, state)
        }

        var rent = 0L
        state.ownedProperties.forEach { owned ->
            val propItem = realEstateMarket.find { it.id == owned.propertyId }
            if (propItem != null) {
                val isSultan = owned.condition == 100 && owned.currentEstimatedValue > propItem.basePrice
                val multiplier = if (isSultan) 1.5 else (owned.condition / 100.0)
                rent += (propItem.baseRentalIncome * multiplier).toLong()
            }
        }

        var dividends = 0.0
        state.ownedStocks.forEach { owned ->
            val liveStock = stockList.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val isIndo = owned.ticker.contains(".JK")
                val currentPriceUsd = liveStock.currentPrice
                val stats = com.example.data.getMarketStats(liveStock)
                dividends += (owned.shares * currentPriceUsd) * (stats.dividendYield / 100.0 / 12.0)
            }
        }

        var cryptoProfit = 0L
        state.ownedCrypto.forEach { owned ->
            val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
            val profit = (livePrice - owned.averagePrice) * owned.amount
            cryptoProfit += profit.toLong()
        }

        EarningsReport(business, rent, dividends.toLong(), cryptoProfit)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = EarningsReport()
    )

    init { 
        try {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Safe Initialization inside IO Thread
                    _playerState.value = loadState()
                    _realEstateMarket.value = loadCustomProperties()
                    _collectionList.value = loadCustomCollections()
                    autoResolveMissingHousing(_playerState.value)
                    
                    val initialStocks = generateStockData()
                    _stockList.value = initialStocks
                    initialStocks.forEach { initialPrices[it.ticker] = it.currentPrice }

                    val currentState = _playerState.value
                    val now = System.currentTimeMillis()
                    val elapsedMs = now - currentState.lastSavedTimeMs
                    
                    // Idle Ratio: 1 Real Life Day (86,400,000 ms) = 1 In-Game Year (12 Months)
                    // Which means 1 offline In-Game Month = 7,200,000 ms (2 Real Hours)
                    val offlineMonthMs = 7_200_000L
                    
                    if (elapsedMs > offlineMonthMs && currentState.lastSavedTimeMs > 0) {
                        val missedMonths = (elapsedMs / offlineMonthMs).toInt().coerceAtMost(24) // Max 2 In-Game Years progression offline
                        
                        repeat(missedMonths) {
                            processMonthlyTick(true) 
                        }
                        _playerState.value = _playerState.value.copy(lastSavedTimeMs = System.currentTimeMillis())
                        saveState(_playerState.value)
                    } else if (currentState.lastSavedTimeMs > 0) {
                        // Update saved time directly without tick if not enough time has passed
                        _playerState.value = _playerState.value.copy(lastSavedTimeMs = now)
                        saveState(_playerState.value)
                    }

                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        startGameLoop() 
                        startStockMarketLoop()
                        startCryptoMarketLoop()
                        startTycoonMarketLoop()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Init error in IO thread: ${e.message}")
                    resetGameProgress()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDebug", "Init error: ${e.message}")
            resetGameProgress()
        }
    }

    private val _newsFeed = MutableStateFlow(listOf(
        MarketNews("0", "Sesi Pasar dibuka. Seluruh pasar global dan domestik beroperasi normal.", "NEUTRAL")
    ))
    val newsFeed: StateFlow<List<MarketNews>> = _newsFeed.asStateFlow()

    private var newsCounter = 1

    private enum class MarketTrend {
        BULL_MARKET, BEAR_MARKET, STEADY_GROWTH, STEADY_BLEED, THE_LOST_DECADE, LONG_TERM_CYCLICAL, WHIPSAW_TRAP
    }

    private data class StockTrendState(
        var currentTrend: MarketTrend,
        var durationLeftMs: Long,
        var cyclePhase: Int = 1
    )

    private val stockTrends = mutableMapOf<String, StockTrendState>()

    private fun assignNewTrend(ticker: String, previousTrend: MarketTrend?): StockTrendState {
        val monthMs = _monthDurationSeconds.value * 1000L
        val yearMs = 12 * monthMs
        
        // Post-Crash Whipsaw Rule
        if (previousTrend == MarketTrend.BEAR_MARKET && Math.random() < 0.60) {
            return StockTrendState(MarketTrend.WHIPSAW_TRAP, (Math.random() * yearMs).toLong())
        }
        
        // Forced follow-up after Whipsaw Trap
        if (previousTrend == MarketTrend.WHIPSAW_TRAP) {
            val next = if (Math.random() < 0.5) MarketTrend.STEADY_BLEED else MarketTrend.BEAR_MARKET
            val dur = ((if(next == MarketTrend.BEAR_MARKET) 1 else 2) + Math.random() * 2) * yearMs
            return StockTrendState(next, dur.toLong())
        }
        
        // Standard distribution
        val rand = Math.random()
        val (trend, durationYears) = when {
            rand < 0.15 -> MarketTrend.BULL_MARKET to (1 + Math.random() * 2)
            rand < 0.30 -> MarketTrend.BEAR_MARKET to (1 + Math.random() * 1)
            rand < 0.50 -> MarketTrend.STEADY_GROWTH to (3 + Math.random() * 2)
            rand < 0.70 -> MarketTrend.STEADY_BLEED to (2 + Math.random() * 2)
            rand < 0.85 -> MarketTrend.THE_LOST_DECADE to (5 + Math.random() * 5)
            else -> MarketTrend.LONG_TERM_CYCLICAL to listOf(3.0, 5.0, 7.0, 10.0).random()
        }
        return StockTrendState(trend, (durationYears * yearMs).toLong())
    }

    private fun pushMarketNewsForTransition(ticker: String, name: String, targetTrend: MarketTrend) {
        val (text, type) = when (targetTrend) {
            MarketTrend.BULL_MARKET -> "INSIDER TIP: Laporan keuangan $ticker ($name) sangat positif! Broker memprediksi lonjakan harga tajam." to "BULL"
            MarketTrend.BEAR_MARKET -> "PANIC SELL: Skandal internal melanda $ticker ($name). Harga diprediksi akan terjun bebas!" to "BEAR"
            MarketTrend.THE_LOST_DECADE -> "ANALISIS: Prospek $ticker ($name) dinilai stagnan untuk beberapa tahun ke depan. Pasar merespon dingin." to "NEUTRAL"
            MarketTrend.WHIPSAW_TRAP -> "BREAKING: $ticker ($name) tiba-tiba meroket keras hari ini! Momen kebangkitan atau hanya jebakan banteng (Bull Trap)?" to "BULL"
            MarketTrend.LONG_TERM_CYCLICAL -> "MARKET WATCH: $ticker ($name) berpotensi mengalami volatilitas ekstrem. Awas ayunan harga yang liar!" to "NEUTRAL"
            else -> return // STEADY_GROWTH, STEADY_BLEED do not need to spam news
        }
        
        val newsItem = MarketNews(
            id = newsCounter.toString(),
            text = text,
            type = type
        )
        newsCounter++
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    private fun startStockMarketLoop() {
        viewModelScope.launch {
            while (true) {
                val delayMs = (_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L)
                delay(delayMs)
                
                val volatility = _marketVolatilityFactor.value
                val monthMs = _monthDurationSeconds.value * 1000L
                
                val updatedList = _stockList.value.map { stock ->
                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                    
                    var trendState = stockTrends[stock.ticker]
                    var newlyTransitioned = false
                    val oldTrend = trendState?.currentTrend
                    
                    if (trendState == null || trendState.durationLeftMs <= 0) {
                        trendState = assignNewTrend(stock.ticker, oldTrend)
                        stockTrends[stock.ticker] = trendState
                        newlyTransitioned = true
                    } else {
                        trendState.durationLeftMs -= delayMs
                    }
                    
                    var baseDrift = 0.0
                    var randVolatility = 0.0
                    
                    when (trendState.currentTrend) {
                        MarketTrend.BULL_MARKET -> {
                            baseDrift = 0.003
                            randVolatility = 0.012
                        }
                        MarketTrend.BEAR_MARKET -> {
                            baseDrift = -0.004
                            randVolatility = 0.015
                        }
                        MarketTrend.STEADY_GROWTH -> {
                            baseDrift = 0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.STEADY_BLEED -> {
                            baseDrift = -0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.THE_LOST_DECADE -> {
                            baseDrift = 0.0
                            randVolatility = 0.001
                        }
                        MarketTrend.LONG_TERM_CYCLICAL -> {
                            val cyclePhase = (trendState.durationLeftMs / monthMs).toInt() % 2
                            baseDrift = if (cyclePhase == 0) 0.008 else -0.008
                            randVolatility = 0.018
                        }
                        MarketTrend.WHIPSAW_TRAP -> {
                            baseDrift = 0.006
                            randVolatility = 0.025
                        }
                    }
                    
                    // Calculation with Game Settings Volatility integrated
                    val changePct = baseDrift + ((Math.random() - 0.5) * randVolatility)
                    val changeApplied = changePct * volatility
                    
                    // Zero Price Bug Recovery
                    val safeCurrentPrice = if (stock.currentPrice <= 0.00) 0.05 else stock.currentPrice
                    val calculatedPrice = safeCurrentPrice * (1 + changeApplied)
                    val newPrice = Math.max(0.01, calculatedPrice)
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    val newHistory = (stock.priceHistory + newPrice).takeLast(40)
                    
                    // Alert system hook logic
                    if (newlyTransitioned && Math.random() < 0.35) {
                        pushMarketNewsForTransition(stock.ticker, stock.name, trendState.currentTrend)
                    }

                    stock.copy(
                        currentPrice = newPrice,
                        changeAbsolute = newChangeAbs,
                        changePercentage = newChangePct,
                        priceHistory = newHistory
                    )
                }.toMutableList()

                _stockList.value = updatedList
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while(true) {
                delay(100) // update setiap 100ms
                updateProgress()
            }
        }
    }

    private fun updateProgress() {
        val currentState = _playerState.value
        val now = System.currentTimeMillis()
        
        // Process Active Upgrades for regular businesses
        var businessesChanged = false
        val newBusinesses = currentState.ownedBusinesses.map { business ->
            var updatedBusiness = business
            if (business.catalogId == "content_creator") {
                businessesChanged = true
                val addedProgress = 1f / 1200f // 120 seconds cycle, update is every 100ms (0.1s)
                var newProgress = business.contentCreatorProgress + addedProgress
                var newSubs = business.contentCreatorSubscribers
                var newCash = business.contentCreatorCash
                if (newProgress >= 1f) {
                    newProgress = 0f
                    var income = (newSubs * 0.05).toLong()
                    val multiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
                    income = (income * multiplier).toLong()
                    
                    newSubs += (business.level * (business.contentCreatorEmployees + 1) * (5..15).random())
                    
                    if (business.level >= 61) {
                        if ((1..100).random() < 10) {
                            val brandBonus = (100_000..500_000).random().toLong() * (business.level / 10)
                            newCash += brandBonus
                        }
                    }
                    newCash += income
                }
                updatedBusiness = updatedBusiness.copy(
                    contentCreatorProgress = newProgress,
                    contentCreatorSubscribers = newSubs,
                    contentCreatorCash = newCash
                )
            }

            val completedUpgrades = updatedBusiness.activeUpgrades.filter { now >= it.finishTimeMs }
            if (completedUpgrades.isNotEmpty()) {
                businessesChanged = true
                var newUpgradeLevels = updatedBusiness.upgradeLevels
                var newPurchasedUpgrades = updatedBusiness.purchasedUpgrades
                var levelsGained = 0
                completedUpgrades.forEach { upgrade ->
                    newUpgradeLevels = newUpgradeLevels + (upgrade.selectedUpgradeId to upgrade.targetLevel)
                    newPurchasedUpgrades = newPurchasedUpgrades + upgrade.selectedUpgradeId
                    levelsGained += 1
                }
                updatedBusiness.copy(
                    activeUpgrades = updatedBusiness.activeUpgrades.filter { now < it.finishTimeMs },
                    upgradeLevels = newUpgradeLevels,
                    purchasedUpgrades = newPurchasedUpgrades,
                    level = updatedBusiness.level + levelsGained
                )
            } else updatedBusiness
        }

        // Process Active Upgrades for holding companies
        var holdingsChanged = false
        val newHoldings = currentState.holdingCompanies.map { holding ->
            var holdingSelfChanged = false
            val newSubs = holding.subsidiaries.map { business ->
                var updatedBusiness = business
                if (business.catalogId == "content_creator") {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    val addedProgress = 1f / 1200f
                    var newProgress = business.contentCreatorProgress + addedProgress
                    var newSubsCount = business.contentCreatorSubscribers
                    var newCash = business.contentCreatorCash
                    if (newProgress >= 1f) {
                        newProgress = 0f
                        var income = (newSubsCount * 0.05).toLong()
                        val multiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
                        income = (income * multiplier).toLong()
                        newSubsCount += (business.level * (business.contentCreatorEmployees + 1) * (5..15).random())
                        if (business.level >= 61) {
                            if ((1..100).random() < 10) {
                                val brandBonus = (100_000..500_000).random().toLong() * (business.level / 10)
                                newCash += brandBonus
                            }
                        }
                        newCash += income
                    }
                    updatedBusiness = updatedBusiness.copy(
                        contentCreatorProgress = newProgress,
                        contentCreatorSubscribers = newSubsCount,
                        contentCreatorCash = newCash
                    )
                }

                val completedUpgrades = updatedBusiness.activeUpgrades.filter { now >= it.finishTimeMs }
                if (completedUpgrades.isNotEmpty()) {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    var newUpgradeLevels = updatedBusiness.upgradeLevels
                    var newPurchasedUpgrades = updatedBusiness.purchasedUpgrades
                    var levelsGained = 0
                    completedUpgrades.forEach { upgrade ->
                        newUpgradeLevels = newUpgradeLevels + (upgrade.selectedUpgradeId to upgrade.targetLevel)
                        newPurchasedUpgrades = newPurchasedUpgrades + upgrade.selectedUpgradeId
                        levelsGained += 1
                    }
                    updatedBusiness.copy(
                        activeUpgrades = updatedBusiness.activeUpgrades.filter { now < it.finishTimeMs },
                        upgradeLevels = newUpgradeLevels,
                        purchasedUpgrades = newPurchasedUpgrades,
                        level = updatedBusiness.level + levelsGained
                    )
                } else updatedBusiness
            }
            if (holdingSelfChanged) holding.copy(subsidiaries = newSubs) else holding
        }

        if (businessesChanged || holdingsChanged) {
            _playerState.value = currentState.copy(
                ownedBusinesses = if (businessesChanged) newBusinesses else currentState.ownedBusinesses,
                holdingCompanies = if (holdingsChanged) newHoldings else currentState.holdingCompanies
            )
        }

        val durationMs = _monthDurationSeconds.value * 1000f
        val step = 100f / durationMs // 100ms from game loop rate
        val newProgress = _monthProgress.value + step

        if (newProgress >= 1f) {
            processMonthlyTick()
        } else {
            _monthProgress.value = newProgress
        }
    }

    private fun processThemeParkBiddingsMonthly(biddings: List<com.example.data.ActiveBidding>): List<com.example.data.ActiveBidding> {
        return biddings.map { bidding ->
            if (bidding.monthsLeft > 0) {
                bidding.copy(monthsLeft = bidding.monthsLeft - 1)
            } else {
                when (bidding.phase) {
                    com.example.data.BiddingPhase.WAITING_INITIAL -> {
                        val rand = (1..100).random()
                        if (rand <= 30) {
                            bidding.copy(phase = com.example.data.BiddingPhase.DEAL_REACHED)
                        } else {
                            val multiplier = (120..180).random() / 100.0
                            val newAsk = (bidding.currentAskingPrice * multiplier).toLong()
                            bidding.copy(phase = com.example.data.BiddingPhase.OWNER_COUNTERED, currentAskingPrice = newAsk)
                        }
                    }
                    com.example.data.BiddingPhase.WAITING_REPLY -> {
                        val offerRatio = bidding.playerOffer.toDouble() / bidding.currentAskingPrice
                        val successChance = when {
                            offerRatio >= 1.2 -> 95
                            offerRatio >= 1.0 -> 70
                            else -> 40
                        }
                        val rand = (1..100).random()
                        if (rand <= successChance) {
                            bidding.copy(phase = com.example.data.BiddingPhase.DEAL_REACHED)
                        } else {
                            val multiplier = (105..130).random() / 100.0
                            val newAsk = (bidding.currentAskingPrice * multiplier).toLong()
                            bidding.copy(phase = com.example.data.BiddingPhase.OWNER_COUNTERED, currentAskingPrice = newAsk)
                        }
                    }
                    else -> bidding
                }
            }
        }.filter { it.phase != com.example.data.BiddingPhase.REJECTED }
    }

    private fun processThemeParkMonthly(branches: List<ThemeParkBranch>, currentMonth: Int): Pair<List<ThemeParkBranch>, Long> {
        val seasonMultiplier = when (currentMonth) {
            6, 7 -> 3.0
            12 -> 4.0
            2, 9 -> 0.7
            else -> 1.0
        }

        var branchNetProfit = 0L
        val updatedBranches = branches.map { branch ->
            val updatedBranch = ThemeParkEngine.processMonthlyTick(branch, seasonMultiplier)
            branchNetProfit += updatedBranch.lastMonthProfit
            updatedBranch
        }
        return Pair(updatedBranches, branchNetProfit)
    }

    private fun processHospitalityMonthly(properties: List<com.example.data.HotelProperty>, currentMonth: Int): Pair<List<com.example.data.HotelProperty>, Long> {
        var totalHospitalityProfit = 0L
        val updatedProperties = properties.map { hotel ->
            var h = hotel
            if (h.isConstructing) {
                if (h.remainingBuildMonths > 0) {
                    val newLeft = h.remainingBuildMonths - 1
                    h = h.copy(
                        remainingBuildMonths = newLeft,
                        isConstructing = newLeft > 0
                    )
                }
                return@map h
            }

            // 1. Kalkulasi Seasonality (Bulan liburan turis membludak)
            val seasonBonus = when(currentMonth) { 12, 1, 7, 8 -> 0.3; 2, 9 -> -0.2; else -> 0.0 }

            val hotelPrestige = h.tier.baseBuildCost / 1_000_000 + h.builtFacilities.size * 10 
            
            // RNG Bencana atau Event Raksasa (Mega Events)
            val rng = (1..100).random()
            var eventOccupancyMultiplier = 1.0
            if (h.builtFacilities.contains(com.example.data.HotelFacility.CONVENTION_CENTER) && rng > 95) {
                h = h.copy(activeMegaEvent = "Tuan Rumah KTT G20!")
                eventOccupancyMultiplier = 2.0 // Sangat penuh
            } else if (rng < 3) {
                h = h.copy(activeMegaEvent = "Skandal Keracunan Makanan")
                eventOccupancyMultiplier = 0.3 // Drop parah
            } else {
                h = h.copy(activeMegaEvent = null)
            }

            var totalHotelRevenue = 0L
            var facilityBonus = h.builtFacilities.sumOf { it.bonusOccupancy }
            if (h.location.startsWith("Integrasi:")) {
                facilityBonus += 0.15
            }
            
            // Default configuration for old saves
            if (h.roomConfigs == null) {
                h.roomConfigs = mutableMapOf()
            }
            if (h.roomConfigs!!.isEmpty()) {
                val sc = com.example.data.RoomClassConfig(isEnabled = true, allocationPercent = 100.0, customPrice = h.tier.baseRoomRate)
                h.roomConfigs!!["STANDARD"] = sc
            }
            
            var sumOccupancy = 0.0
            var sumAllocation = 0.0

            for ((classKey, config) in h.roomConfigs!!) {
                if (!config.isEnabled || config.allocationPercent <= 0.0) continue
                sumAllocation += config.allocationPercent
                
                val roomClassEnum = try { com.example.data.RoomClassStrategy.valueOf(classKey) } catch(e: Exception) { continue }
                val allocatedRooms = (h.tier.maxRooms * (config.allocationPercent / 100.0)).toInt()
                
                val baseRate = (h.tier.baseRoomRate * roomClassEnum.priceMultiplier).toLong()
                val pricePenalty = ((config.customPrice - baseRate).toDouble() / baseRate) * 0.5 
                val prestigeDeficit = maxOf(0, roomClassEnum.requiredPrestige - hotelPrestige.toInt())
                val prestigePenalty = (prestigeDeficit * 0.02) 
                
                var finalOccupancy = 0.6 + seasonBonus + facilityBonus - pricePenalty - prestigePenalty
                
                if (h.activeMegaEvent == "Tuan Rumah KTT G20!") {
                    finalOccupancy = 1.0
                } else if (h.activeMegaEvent == "Skandal Keracunan Makanan") {
                    finalOccupancy *= 0.3
                }
                
                finalOccupancy = finalOccupancy.coerceIn(0.05, 1.0)
                config.lastMonthOccupancy = finalOccupancy
                
                val occupiedRooms = (allocatedRooms * finalOccupancy).toInt()
                val avgGuestsPerRoom = when(roomClassEnum) {
                    com.example.data.RoomClassStrategy.STANDARD, com.example.data.RoomClassStrategy.SUPERIOR -> 1.5
                    com.example.data.RoomClassStrategy.DELUXE, com.example.data.RoomClassStrategy.JUNIOR_SUITE -> 2.0
                    com.example.data.RoomClassStrategy.SUITE, com.example.data.RoomClassStrategy.PRESIDENTIAL -> 3.0
                }
                val totalGuestsForThisClass = (occupiedRooms * avgGuestsPerRoom * 30).toLong()
                
                val roomRevenue = (occupiedRooms * 30L) * config.customPrice
                val spendMultiplier = roomClassEnum.priceMultiplier
                val facilityRevenuePerGuest = h.builtFacilities.sumOf { it.bonusRevenue } / 1000L
                val facilityRevenueForThisClass = totalGuestsForThisClass * facilityRevenuePerGuest * spendMultiplier.toLong()
                
                config.lastMonthRevenue = roomRevenue + facilityRevenueForThisClass
                totalHotelRevenue += config.lastMonthRevenue
                
                sumOccupancy += finalOccupancy * (config.allocationPercent / 100.0)
            }
            
            if (sumAllocation > 0) {
                h = h.copy(lastMonthOccupancyRate = sumOccupancy / (sumAllocation / 100.0))
            } else {
                h = h.copy(lastMonthOccupancyRate = 0.0)
            }

            // D. Hitung Keuangan
            var totalStaff = 10
            var roomOperationalExpense = 0L
            
            for ((classKey, config) in h.roomConfigs!!) {
                if (!config.isEnabled || config.allocationPercent <= 0.0) continue
                
                val roomClassEnum = try { com.example.data.RoomClassStrategy.valueOf(classKey) } catch(e: Exception) { continue }
                val allocatedRooms = (h.tier.maxRooms * (config.allocationPercent / 100.0)).toInt()
                
                val staffRatio = when(roomClassEnum) {
                    com.example.data.RoomClassStrategy.STANDARD, com.example.data.RoomClassStrategy.SUPERIOR -> 10
                    com.example.data.RoomClassStrategy.DELUXE, com.example.data.RoomClassStrategy.JUNIOR_SUITE -> 5
                    com.example.data.RoomClassStrategy.SUITE, com.example.data.RoomClassStrategy.PRESIDENTIAL -> 2
                }
                totalStaff += maxOf(1, allocatedRooms / staffRatio)
                
                val utilityCostPerRoom = (roomClassEnum.priceMultiplier * 15L).toLong()
                roomOperationalExpense += (allocatedRooms * utilityCostPerRoom * 30L)
            }
            
            val avgSalary = 2000L + (h.tier.baseRoomRate * 5)
            val staffExpense = totalStaff * avgSalary
            
            var facilityMaintenanceExpense = 0L
            for (facility in h.builtFacilities) {
                facilityMaintenanceExpense += facility.maintenanceCost
                totalStaff += 5
            }
            
            val propertyTotalExpense = staffExpense + roomOperationalExpense + facilityMaintenanceExpense
            val propertyTotalRevenue = totalHotelRevenue

            var revenue = propertyTotalRevenue

            if (h.activeMegaEvent == "Tuan Rumah KTT G20!") revenue += 50_000_000 // Bonus raksasa

            val profit = revenue - propertyTotalExpense
            h = h.copy(lastMonthRevenue = revenue, lastMonthExpense = propertyTotalExpense)
            totalHospitalityProfit += profit

            h
        }
        return Pair(updatedProperties, totalHospitalityProfit)
    }

    private fun processMonthlyTick(isOffline: Boolean = false) {
        _monthProgress.value = 0f
        val currentState = _playerState.value
        var monthlyIncome = 0L
        var monthlyExpenses = 0L

        // Business Income & Movie Progression
        val mappedBusinesses = currentState.ownedBusinesses.map { owned ->
            try {
                var businessInternalRevenue = 0L
                var businessInternalExpenses = 0L
                
                var isUpgradingNow = owned.isUpgrading
                var upgradeDelayNow = owned.upgradeDelayMonths
                if (isUpgradingNow) {
                    upgradeDelayNow -= 1
                    if (upgradeDelayNow <= 0) {
                        isUpgradingNow = false
                        upgradeDelayNow = 0
                    }
                }
                
                if (!owned.isUpgrading) {
                    val catalogItem = getCatalogItem(owned.catalogId, currentState)
                    if (catalogItem?.category == com.example.data.BusinessCategory.AVIATION) {
                        val processed = processAviationMonthlyTick(owned)
                        val lastMonthRev = processed.customRevenue ?: 0L
                        val lastMonthExp = calculateAviationExpenses(processed)
                        val netProfit = lastMonthRev - lastMonthExp
                        val (newCompanyCash, dividendToGlobal) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                        
                        if (dividendToGlobal < 0) {
                            monthlyExpenses += (-dividendToGlobal)
                        } else {
                            monthlyIncome += dividendToGlobal
                        }
                        
                        var isUpgradingNow = processed.isUpgrading
                        var upgradeDelayNow = processed.upgradeDelayMonths
                        if (isUpgradingNow) {
                            upgradeDelayNow -= 1
                            if (upgradeDelayNow <= 0) {
                                isUpgradingNow = false
                                upgradeDelayNow = 0
                            }
                        }
                        
                        return@map processed.copy(
                            companyCash = newCompanyCash,
                            isUpgrading = isUpgradingNow,
                            upgradeDelayMonths = upgradeDelayNow
                        )
                    }

                    if (catalogItem?.category == com.example.data.BusinessCategory.CRUISE_LINE) {
                        val processed = processCruiseMonthlyTick(owned)
                        val netProfit = calculateCruiseNetProfit(processed)
                        val (newCompanyCash, dividendToGlobal) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                        
                        if (dividendToGlobal < 0) {
                            monthlyExpenses += (-dividendToGlobal)
                        } else {
                            monthlyIncome += dividendToGlobal
                        }
                        
                        var isUpgradingNow = processed.isUpgrading
                        var upgradeDelayNow = processed.upgradeDelayMonths
                        if (isUpgradingNow) {
                            upgradeDelayNow -= 1
                            if (upgradeDelayNow <= 0) {
                                isUpgradingNow = false
                                upgradeDelayNow = 0
                            }
                        }
                        
                        return@map processed.copy(
                            companyCash = newCompanyCash,
                            isUpgrading = isUpgradingNow,
                            upgradeDelayMonths = upgradeDelayNow
                        )
                    }

                    var newFleet = owned.airlineFleet
                    var newMarketDemand = owned.marketDemand
                    if (owned.acquiredStockTicker != null) {
                        val acquiredTicker = owned.acquiredStockTicker
                        val liveStock = _stockList.value.find { it.ticker == acquiredTicker }
                        val stockInPortfolio = currentState.ownedStocks.find { s -> s.ticker == acquiredTicker }
                        if (liveStock != null && stockInPortfolio != null && owned.instanceId != currentState.taxLegalReport.frozenBusinessId) {
                            val stats = com.example.data.getMarketStats(liveStock)
                            val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0
                            val estMonthlyDividend = (stockInPortfolio.shares * liveStock.currentPrice * monthlyYieldPercent).toLong()
                            businessInternalRevenue += estMonthlyDividend
                        }
                    } else if (catalogItem != null) {
                        val (baseRev, baseMaint) = getBusinessStats(owned, catalogItem, currentState)
                        val revenue = if (owned.instanceId == currentState.taxLegalReport.frozenBusinessId) {
                            0L // Frozen
                        } else if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY || catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING || catalogItem.category == com.example.data.BusinessCategory.CRUISE_LINE) {
                            0L // Handled explicitly
                        } else if (catalogItem.isFluctuating) {
                            (baseRev / 2 .. baseRev * 2).random()
                        } else {
                            baseRev
                        }
                        
                        businessInternalRevenue += revenue
                        businessInternalExpenses += if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY || catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING || catalogItem.category == com.example.data.BusinessCategory.CRUISE_LINE) 0L else baseMaint
                    }
                }
                
                // Aggregate Net Profit of all subsidiaries globally
                val updatedSubsidiaries = owned.subsidiaries.map { sub ->
                    var updatedSub = sub
                    if (updatedSub.isUpgrading) {
                        val newDelay = updatedSub.upgradeDelayMonths - 1
                        if (newDelay <= 0) {
                            var newLevel = updatedSub.level
                            var newMichelin = updatedSub.michelinStars
                            if (updatedSub.pendingAction == "LEVEL_UP") {
                                newLevel += 1
                            } else if (updatedSub.pendingAction == "MICHELIN_HUNT") {
                                newMichelin = kotlin.math.min(3, newMichelin + (0..1).random())
                            }
                            updatedSub = updatedSub.copy(
                                isUpgrading = false,
                                upgradeDelayMonths = 0,
                                pendingAction = null,
                                level = newLevel,
                                michelinStars = newMichelin
                            )
                        } else {
                            updatedSub = updatedSub.copy(upgradeDelayMonths = newDelay)
                        }
                    }
                    
                    if (updatedSub.catalogId == "RESTAURANT_BRANCH") {
                        val baseIncome = 15000L
                        val netProfit = (baseIncome * updatedSub.level) * (1.0 + (updatedSub.michelinStars * 1.5))
                        businessInternalRevenue += netProfit.toLong()
                    } else {
                        val subCatalog = getCatalogItem(updatedSub.catalogId, currentState)
                        if (subCatalog != null) {
                            val (baseSubRev, baseSubMaint) = getBusinessStats(updatedSub, subCatalog, currentState)
                            val subRev = if (subCatalog.isFluctuating) (baseSubRev / 2 .. baseSubRev * 2).random() else baseSubRev
                            businessInternalRevenue += subRev
                            businessInternalExpenses += baseSubMaint
                        }
                    }
                    updatedSub
                }

                var extraV = owned.extraValuation
                var totalStreamingIncome = 0L
                val rawHistory = if (owned.projectHistory != null) owned.projectHistory else emptyList()
                val updatedHistory = rawHistory.map { p ->
                    var proj = p
                    if (proj.productionPhase == "ANTREAN") {
                        val sy = proj.scheduledYear
                        val sm = proj.scheduledMonth
                        if (sy != null && sm != null && (currentState.inGameYear > sy || (currentState.inGameYear == sy && currentState.inGameMonth >= sm))) {
                            proj = proj.copy(productionPhase = "Pra-Produksi")
                        }
                    }

                    if (proj.productionPhase == "ANTREAN") {
                        proj
                    } else if (proj.status != "FINISHED" && proj.status != "IN_THEATERS") {
                        if (proj.productionDelayMonths > 0) {
                            val newDelay = proj.productionDelayMonths - 1
                            
                            val isAnim = owned.studioType == "ANIMATION"
                            val estTotal = when {
                                isAnim && proj.filmFormat == "Short Film" -> 19.0
                                isAnim -> 42.0
                                proj.filmFormat == "Short Film" -> 4.0
                                else -> 18.0
                            } + kotlin.math.min((proj.budget / 10000000).toInt(), 12).toDouble()
                            
                            val currentTotal = kotlin.math.max(newDelay.toDouble(), estTotal)
                            val progressRatio = (currentTotal - newDelay) / currentTotal
                            
                            val newPhase = when {
                                progressRatio < 0.25 -> "Pra-Produksi"
                                progressRatio < 0.85 -> if (isAnim) "Produksi Animasi" else "Syuting"
                                else -> "Pasca Produksi" 
                            }
                                
                            if (newDelay <= 0) {
                                var currentScore = proj.internalScore
                                if (currentScore == null) {
                                    val budgetPenalty = (proj.budget / 10000000).toInt()
                                    currentScore = (20..80).random() + (budgetPenalty * 2)
                                }
                                currentScore = currentScore.coerceIn(1, 99)
                                proj.copy(
                                    productionPhase = "Quality Control",
                                    productionDelayMonths = 0,
                                    isQcPhase = true,
                                    internalScore = currentScore
                                )
                            } else {
                                proj.copy(
                                    productionPhase = newPhase,
                                    productionDelayMonths = newDelay
                                )
                            }
                        } else if (proj.isAwaitingRelease) {
                            try {
                                val dateParts = proj.scheduledReleaseDate?.split("/")
                                if (dateParts != null && dateParts.size >= 2) {
                                    val sM = dateParts[0].toIntOrNull() ?: 1
                                    val sY = dateParts[1].toIntOrNull() ?: 2026
                                    if (currentState.inGameYear > sY || (currentState.inGameYear == sY && currentState.inGameMonth >= sM)) {
                                        proj.copy(
                                            productionPhase = "TAYANG",
                                            status = "IN_THEATERS",
                                            isAwaitingRelease = false
                                        )
                                    } else proj
                                } else {
                                    proj.copy(
                                        productionPhase = "TAYANG",
                                        status = "IN_THEATERS",
                                        isAwaitingRelease = false
                                    )
                                }
                            } catch (e: Exception) {
                                proj.copy(
                                    productionPhase = "TAYANG",
                                    status = "IN_THEATERS",
                                    isAwaitingRelease = false
                                )
                            }
                        } else if (proj.isQcPhase) {
                            proj
                        } else {
                            if (proj.internalScore == null) {
                                val budgetPenalty = (proj.budget / 10000000).toInt()
                                var score = (20..80).random() + (budgetPenalty * 2)
                                score = score.coerceIn(1, 99)
                                proj.copy(
                                    productionPhase = "Quality Control",
                                    productionDelayMonths = 0,
                                    isQcPhase = true,
                                    internalScore = score
                                )
                            } else {
                                proj.copy(
                                    productionPhase = "TAYANG",
                                    status = "IN_THEATERS"
                                )
                            }
                        }
                    } else if (proj.status == "IN_THEATERS" && proj.remainingMonths > 0) {
                        val thisPayout = if (proj.remainingMonths == 1) {
                            proj.targetMaxRevenue - proj.currentRevenue
                        } else {
                            val init = if (proj.distributionScale == "Global") 6 else 4
                            val calc = proj.targetMaxRevenue / init
                            if (calc > 0) calc else 0L
                        }
                        val newCurrentRev = proj.currentRevenue + thisPayout
                        val newRemaining = proj.remainingMonths - 1
                        val newStatus = if (newRemaining <= 0) "FINISHED" else "IN_THEATERS"
                        
                        businessInternalRevenue += thisPayout
                        if (newStatus == "FINISHED") {
                            extraV += maxOf(0L, proj.netProfit)
                        }
                        
                        proj.copy(
                            remainingMonths = newRemaining,
                            currentRevenue = newCurrentRev,
                            boxOffice = newCurrentRev,
                            status = newStatus,
                            releaseMonth = if (newStatus == "FINISHED") currentState.inGameMonth else null,
                            releaseYear = if (newStatus == "FINISHED") currentState.inGameYear else null
                        )
                    } else if (proj.status == "FINISHED") {
                        if (proj.licenseRemainingMonths != null && proj.licenseRemainingMonths!! > 0) {
                            totalStreamingIncome += proj.licenseMonthlyFee ?: 0L
                            val updatedRemaining = proj.licenseRemainingMonths!! - 1
                            if (updatedRemaining == 0) {
                                proj.copy(
                                    licenseRemainingMonths = null,
                                    licenseeName = null,
                                    licenseMonthlyFee = null
                                )
                            } else {
                                proj.copy(licenseRemainingMonths = updatedRemaining)
                            }
                        } else {
                            proj
                        }
                    } else proj
                }
                
                businessInternalRevenue += totalStreamingIncome
                
                var claimModifiers = 0.0
                var totalDividendsFromHealthcare = 0L
                val updatedHealthcareUnits = owned.healthcareSubsidiaries.map { unit ->
                    var currentUnit = unit
                    var unitRev = 0.0
                    
                    if (currentUnit.isUpgrading) {
                        val newDelay = currentUnit.upgradeDelayMonths - 1
                        if (newDelay <= 0) {
                            currentUnit = currentUnit.copy(isUpgrading = false, upgradeDelayMonths = 0)
                        } else {
                            currentUnit = currentUnit.copy(upgradeDelayMonths = newDelay)
                        }
                    }
                    
                    if (!currentUnit.isUpgrading) {
                        if (currentUnit.type == "HOSPITAL") {
                            val newPatients = (100 * currentUnit.level).toLong()
                            currentUnit = currentUnit.copy(members = newPatients)
                            unitRev = newPatients * 5000.0 // Approx $5k per patient / mo
                            
                            extraV += (currentUnit.level * 2_000_000L)
                        } else if (currentUnit.type == "INSURANCE") {
                            val premiumMultiplier = when (currentUnit.tierCategory) {
                                "PREMIUM" -> 1.5
                                "ELITE" -> 2.5
                                else -> 1.0
                            }
                            val growth = (100..500).random() * currentUnit.level
                            currentUnit = currentUnit.copy(members = currentUnit.members + growth)
                            unitRev = currentUnit.members * 50.0 * premiumMultiplier
                            
                            val riskProb = when (currentUnit.tierCategory) {
                                "PREMIUM" -> 0.08
                                "ELITE" -> 0.12
                                else -> 0.05
                            }
                            
                            if (kotlin.random.Random.nextDouble() < riskProb) {
                                val membersMod = currentUnit.members / 1000.0
                                val claimDeduction = (1_000_000L..10_000_000L).random().toDouble() * maxOf(1.0, membersMod) * premiumMultiplier
                                if (currentUnit.unitCash >= claimDeduction) {
                                    currentUnit = currentUnit.copy(unitCash = currentUnit.unitCash - claimDeduction)
                                } else {
                                    val remainingClaim = claimDeduction - currentUnit.unitCash
                                    currentUnit = currentUnit.copy(unitCash = 0.0)
                                    claimModifiers += remainingClaim
                                }
                            }
                            
                            extraV += (currentUnit.members * 1_000L) // $1k per member valuation
                        } else if (currentUnit.type == "CLINIC") {
                            val newPatients = (50 * currentUnit.level).toLong()
                            currentUnit = currentUnit.copy(members = newPatients)
                            unitRev = newPatients * 2000.0
                            extraV += (currentUnit.level * 250_000L)
                        }
                        
                        val maintenance = unitRev * 0.3
                        val netUnitProfit = unitRev - maintenance
                        if (netUnitProfit > 0) {
                            val dividend = netUnitProfit * 0.2
                            totalDividendsFromHealthcare += dividend.toLong()
                            currentUnit = currentUnit.copy(unitCash = currentUnit.unitCash + (netUnitProfit - dividend))
                        } else {
                            currentUnit = currentUnit.copy(unitCash = maxOf(0.0, currentUnit.unitCash + netUnitProfit))
                        }
                        
                        currentUnit = currentUnit.copy(monthlyRevenue = unitRev)
                    }
                    currentUnit
                }
                
                businessInternalRevenue += totalDividendsFromHealthcare
                
                val netProfit = businessInternalRevenue - businessInternalExpenses
                val (newCompanyCash, dividendToGlobal) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                
                var finalCompanyCash = newCompanyCash - claimModifiers
                if (finalCompanyCash < 0) finalCompanyCash = 0.0
                val updatedTenders = owned.activeTenders.map { tender ->
                    if (!tender.isFinished && tender.remainingMonths > 0) {
                        val newRemaining = tender.remainingMonths - 1
                        if (newRemaining == 0) {
                            val inject = tender.totalContractValue
                            finalCompanyCash += inject
                            tender.copy(remainingMonths = 0, isFinished = true)
                        } else {
                            tender.copy(remainingMonths = newRemaining)
                        }
                    } else tender
                }

                val updatedThemeParkBiddings = processThemeParkBiddingsMonthly(owned.activeThemeParkBiddings)
                val (updatedThemeParkBranches, parkNetProfit) = processThemeParkMonthly(owned.themeParkBranches, currentState.inGameMonth)
                if (parkNetProfit > 0) {
                    val retainedEarnings = (parkNetProfit * 0.6).toLong()
                    val divToHolding = parkNetProfit - retainedEarnings
                    finalCompanyCash += retainedEarnings
                    monthlyIncome += divToHolding
                } else {
                    finalCompanyCash += parkNetProfit
                    if (finalCompanyCash < 0) {
                        monthlyExpenses += Math.abs(finalCompanyCash.toLong())
                        finalCompanyCash = 0.0
                    }
                }

                val (updatedHospitalityProperties, hospitalityProfit) = processHospitalityMonthly(owned.hospitalityProperties, currentState.inGameMonth)
                if (hospitalityProfit > 0) {
                    val retainedEarnings = (hospitalityProfit * 0.6).toLong()
                    val divToHolding = hospitalityProfit - retainedEarnings
                    finalCompanyCash += retainedEarnings
                    monthlyIncome += divToHolding
                } else {
                    finalCompanyCash += hospitalityProfit
                    if (finalCompanyCash < 0) {
                        monthlyExpenses += Math.abs(finalCompanyCash.toLong())
                        finalCompanyCash = 0.0
                    }
                }

                if (dividendToGlobal < 0) {
                    monthlyExpenses += (-dividendToGlobal)
                } else {
                    monthlyIncome += dividendToGlobal
                }
                
                var newClientProjects = owned.availableClientProjects
                if (owned.catalogId == "construction") {
                    val count = (1..3).random()
                    val generated = mutableListOf<com.example.data.ConstructionProject>()
                    val types = listOf("Pabrik", "Gedung", "Hotel", "Mall", "Apartemen")
                    for (i in 0 until count) {
                        val baseBudget = (500_000L..5_000_000L).random() * owned.level
                        val dur = (3..12).random()
                        val margin = kotlin.random.Random.nextDouble(0.2, 0.4)
                        val finalProfit = (baseBudget * margin).toLong()
                        generated.add(com.example.data.ConstructionProject(
                            name = "Klien: ${types.random()}|$baseBudget|$margin",
                            totalContractValue = finalProfit.toDouble(),
                            durationMonths = dur,
                            remainingMonths = dur
                        ))
                    }
                    newClientProjects = generated
                }

                var newClientEventRequests = owned.clientEventRequests
                if (owned.catalogId == "media_radio") {
                    newClientEventRequests = generateEventRequestsForBusiness(owned)
                }
                
                owned.copy(projectHistory = updatedHistory, extraValuation = extraV, companyCash = finalCompanyCash, activeTenders = updatedTenders, subsidiaries = updatedSubsidiaries, isUpgrading = isUpgradingNow, upgradeDelayMonths = upgradeDelayNow, availableClientProjects = newClientProjects, healthcareSubsidiaries = updatedHealthcareUnits, clientEventRequests = newClientEventRequests, themeParkBranches = updatedThemeParkBranches, activeThemeParkBiddings = updatedThemeParkBiddings, hospitalityProperties = updatedHospitalityProperties)
            } catch (e: Exception) {
                e.printStackTrace()
                owned
            }
        }
        
        // Holding Company Income & Movie Progression
        val mappedHoldings = currentState.holdingCompanies.map { holding ->
            try {
                var newHoldingCashInflow = 0L
                var newHoldingCashOutflow = 0L

                val newSubs = holding.subsidiaries.map { owned ->
                    try {
                        var businessInternalRevenue = 0L
                        var businessInternalExpenses = 0L

                        var isUpgradingNow = owned.isUpgrading
                        var upgradeDelayNow = owned.upgradeDelayMonths
                        if (isUpgradingNow) {
                            upgradeDelayNow -= 1
                            if (upgradeDelayNow <= 0) {
                                isUpgradingNow = false
                                upgradeDelayNow = 0
                            }
                        }
                        
                        val catalogItem = getCatalogItem(owned.catalogId, currentState)
                        if (catalogItem?.category == com.example.data.BusinessCategory.AVIATION) {
                            val processed = processAviationMonthlyTick(owned)
                            val lastMonthRev = processed.customRevenue ?: 0L
                            val lastMonthExp = calculateAviationExpenses(processed)
                            val netProfit = lastMonthRev - lastMonthExp
                            val (newCompanyCash, dividendToHoldingParent) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                            
                            if (dividendToHoldingParent > 0) {
                                newHoldingCashInflow += dividendToHoldingParent
                            } else {
                                newHoldingCashOutflow += (-dividendToHoldingParent)
                            }
                            
                            var isUpgradingNow = processed.isUpgrading
                            var upgradeDelayNow = processed.upgradeDelayMonths
                            if (isUpgradingNow) {
                                upgradeDelayNow -= 1
                                if (upgradeDelayNow <= 0) {
                                    isUpgradingNow = false
                                    upgradeDelayNow = 0
                                }
                            }
                            
                            return@map processed.copy(
                                companyCash = newCompanyCash,
                                isUpgrading = isUpgradingNow,
                                upgradeDelayMonths = upgradeDelayNow
                            )
                        }

                        if (catalogItem?.category == com.example.data.BusinessCategory.CRUISE_LINE) {
                            val processed = processCruiseMonthlyTick(owned)
                            val netProfit = calculateCruiseNetProfit(processed)
                            val (newCompanyCash, dividendToHoldingParent) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                            
                            if (dividendToHoldingParent > 0) {
                                newHoldingCashInflow += dividendToHoldingParent
                            } else {
                                newHoldingCashOutflow += (-dividendToHoldingParent)
                            }
                            
                            var isUpgradingNow = processed.isUpgrading
                            var upgradeDelayNow = processed.upgradeDelayMonths
                            if (isUpgradingNow) {
                                upgradeDelayNow -= 1
                                if (upgradeDelayNow <= 0) {
                                    isUpgradingNow = false
                                    upgradeDelayNow = 0
                                }
                            }
                            
                            return@map processed.copy(
                                companyCash = newCompanyCash,
                                isUpgrading = isUpgradingNow,
                                upgradeDelayMonths = upgradeDelayNow
                            )
                        }

                        var newFleet = owned.airlineFleet
                        var newMarketDemand = owned.marketDemand
                        if (owned.acquiredStockTicker != null) {
                                val acquiredTicker = owned.acquiredStockTicker
                                val liveStock = _stockList.value.find { it.ticker == acquiredTicker }
                                val stockInPortfolio = currentState.ownedStocks.find { it.ticker == acquiredTicker }
                                if (liveStock != null && stockInPortfolio != null && owned.instanceId != currentState.taxLegalReport.frozenBusinessId) {
                                    val stats = com.example.data.getMarketStats(liveStock)
                                    val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0
                                    val estMonthlyDividend = (stockInPortfolio.shares * liveStock.currentPrice * monthlyYieldPercent).toLong()
                                    businessInternalRevenue += estMonthlyDividend
                                }
                            } else if (catalogItem != null) {
                                val (baseRev, baseMaint) = getBusinessStats(owned, catalogItem, currentState)
                                val revenue = if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY || catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING || catalogItem.category == com.example.data.BusinessCategory.CRUISE_LINE) {
                                    0L // Handled explicitly below
                                } else if (catalogItem.isFluctuating) {
                                    (baseRev / 1.5).toLong() // Average projection inside holding
                                } else {
                                    baseRev
                                }
                                val (br, _) = getBusinessStats(owned, catalogItem, currentState) // dummy for layout parity
                                businessInternalRevenue += revenue
                                businessInternalExpenses += if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY || catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING || catalogItem.category == com.example.data.BusinessCategory.CRUISE_LINE) 0L else baseMaint
                            }
                        
                        val updatedDeepSubs = owned.subsidiaries.map { sub ->
                            var updatedSub = sub
                            if (updatedSub.isUpgrading) {
                                val newDelay = updatedSub.upgradeDelayMonths - 1
                                if (newDelay <= 0) {
                                    var newLevel = updatedSub.level
                                    var newMichelin = updatedSub.michelinStars
                                    if (updatedSub.pendingAction == "LEVEL_UP") {
                                        newLevel += 1
                                    } else if (updatedSub.pendingAction == "MICHELIN_HUNT") {
                                        newMichelin = kotlin.math.min(3, newMichelin + (0..1).random())
                                    }
                                    updatedSub = updatedSub.copy(
                                        isUpgrading = false,
                                        upgradeDelayMonths = 0,
                                        pendingAction = null,
                                        level = newLevel,
                                        michelinStars = newMichelin
                                    )
                                } else {
                                    updatedSub = updatedSub.copy(upgradeDelayMonths = newDelay)
                                }
                            }
                            
                            if (updatedSub.catalogId == "RESTAURANT_BRANCH") {
                                val baseIncome = 15000L
                                val netProfit = (baseIncome * updatedSub.level) * (1.0 + (updatedSub.michelinStars * 1.5))
                                businessInternalRevenue += netProfit.toLong()
                            } else {
                                val subCatalog = getCatalogItem(updatedSub.catalogId, currentState)
                                if (subCatalog != null) {
                                    val (baseSubRev, baseSubMaint) = getBusinessStats(updatedSub, subCatalog, currentState)
                                    val subRev = if (subCatalog.isFluctuating) (baseSubRev / 2 .. baseSubRev * 2).random() else baseSubRev
                                    businessInternalRevenue += subRev
                                    businessInternalExpenses += baseSubMaint
                                }
                            }
                            updatedSub
                        }

                        var extraV = owned.extraValuation
                        var totalStreamingIncome = 0L
                        val rawHistory = if (owned.projectHistory != null) owned.projectHistory else emptyList()
                        val updatedHistory = rawHistory.map { p ->
                            var proj = p
                            if (proj.productionPhase == "ANTREAN") {
                                val sy = proj.scheduledYear
                                val sm = proj.scheduledMonth
                                if (sy != null && sm != null && (currentState.inGameYear > sy || (currentState.inGameYear == sy && currentState.inGameMonth >= sm))) {
                                    proj = proj.copy(productionPhase = "Pra-Produksi")
                                }
                            }

                            if (proj.productionPhase == "ANTREAN") {
                                proj
                            } else if (proj.status != "FINISHED" && proj.status != "IN_THEATERS") {
                                if (proj.productionDelayMonths > 0) {
                                    val newDelay = proj.productionDelayMonths - 1
                                    
                                    val isAnim = owned.studioType == "ANIMATION"
                                    val estTotal = when {
                                        isAnim && proj.filmFormat == "Short Film" -> 19.0
                                        isAnim -> 42.0
                                        proj.filmFormat == "Short Film" -> 4.0
                                        else -> 18.0
                                    } + kotlin.math.min((proj.budget / 10000000).toInt(), 12).toDouble()
                                    
                                    val currentTotal = kotlin.math.max(newDelay.toDouble(), estTotal)
                                    val progressRatio = (currentTotal - newDelay) / currentTotal
                                    
                                    val newPhase = when {
                                        progressRatio < 0.25 -> "Pra-Produksi"
                                        progressRatio < 0.85 -> if (isAnim) "Produksi Animasi" else "Syuting"
                                        else -> "Pasca Produksi" 
                                    }
                                        
                                    if (newDelay <= 0) {
                                        var currentScore = proj.internalScore
                                        if (currentScore == null) {
                                            val budgetPenalty = (proj.budget / 10000000).toInt()
                                            currentScore = (20..80).random() + (budgetPenalty * 2)
                                        }
                                        currentScore = currentScore.coerceIn(1, 99)
                                        proj.copy(
                                            productionPhase = "Quality Control",
                                            productionDelayMonths = 0,
                                            isQcPhase = true,
                                            internalScore = currentScore
                                        )
                                    } else {
                                        proj.copy(
                                            productionPhase = newPhase,
                                            productionDelayMonths = newDelay
                                        )
                                    }
                                } else if (proj.isAwaitingRelease) {
                                    try {
                                        val dateParts = proj.scheduledReleaseDate?.split("/")
                                        if (dateParts != null && dateParts.size >= 2) {
                                            val sM = dateParts[0].toIntOrNull() ?: 1
                                            val sY = dateParts[1].toIntOrNull() ?: 2026
                                            if (currentState.inGameYear > sY || (currentState.inGameYear == sY && currentState.inGameMonth >= sM)) {
                                                proj.copy(
                                                    productionPhase = "TAYANG",
                                                    status = "IN_THEATERS",
                                                    isAwaitingRelease = false
                                                )
                                            } else proj
                                        } else {
                                            proj.copy(
                                                productionPhase = "TAYANG",
                                                status = "IN_THEATERS",
                                                isAwaitingRelease = false
                                            )
                                        }
                                    } catch (e: Exception) {
                                        proj.copy(
                                            productionPhase = "TAYANG",
                                            status = "IN_THEATERS",
                                            isAwaitingRelease = false
                                        )
                                    }
                                } else if (proj.isQcPhase) {
                                    proj
                                } else {
                                    if (proj.internalScore == null) {
                                        val budgetPenalty = (proj.budget / 10000000).toInt()
                                        var score = (20..80).random() + (budgetPenalty * 2)
                                        score = score.coerceIn(1, 99)
                                        proj.copy(
                                            productionPhase = "Quality Control",
                                            productionDelayMonths = 0,
                                            isQcPhase = true,
                                            internalScore = score
                                        )
                                    } else {
                                        proj.copy(
                                            productionPhase = "TAYANG",
                                            status = "IN_THEATERS"
                                        )
                                    }
                                }
                            } else if (proj.status == "IN_THEATERS" && proj.remainingMonths > 0) {
                                val thisPayout = if (proj.remainingMonths == 1) {
                                    proj.targetMaxRevenue - proj.currentRevenue
                                } else {
                                    val init = if (proj.distributionScale == "Global") 6 else 4
                                    val calc = proj.targetMaxRevenue / init
                                    if (calc > 0) calc else 0L
                                }
                                val newCurrentRev = proj.currentRevenue + thisPayout
                                val newRemaining = proj.remainingMonths - 1
                                val newStatus = if (newRemaining <= 0) "FINISHED" else "IN_THEATERS"
                                
                                businessInternalRevenue += thisPayout
                                if (newStatus == "FINISHED") {
                                    extraV += maxOf(0L, proj.netProfit)
                                }
                                
                                proj.copy(
                                    remainingMonths = newRemaining,
                                    currentRevenue = newCurrentRev,
                                    boxOffice = newCurrentRev,
                                    status = newStatus,
                                    releaseMonth = if (newStatus == "FINISHED") currentState.inGameMonth else null,
                                    releaseYear = if (newStatus == "FINISHED") currentState.inGameYear else null
                                )
                            } else if (proj.status == "FINISHED") {
                                if (proj.licenseRemainingMonths != null && proj.licenseRemainingMonths!! > 0) {
                                    totalStreamingIncome += proj.licenseMonthlyFee ?: 0L
                                    val updatedRemaining = proj.licenseRemainingMonths!! - 1
                                    if (updatedRemaining == 0) {
                                        proj.copy(
                                            licenseRemainingMonths = null,
                                            licenseeName = null,
                                            licenseMonthlyFee = null
                                        )
                                    } else {
                                        proj.copy(licenseRemainingMonths = updatedRemaining)
                                    }
                                } else {
                                    proj
                                }
                            } else proj
                        }
                        
                        businessInternalRevenue += totalStreamingIncome
                        
                        var claimModifiers = 0.0
                        var totalDividendsFromHealthcare = 0L
                        val updatedHealthcareUnits = owned.healthcareSubsidiaries.map { unit ->
                            var currentUnit = unit
                            var unitRev = 0.0
                            
                            if (currentUnit.isUpgrading) {
                                val newDelay = currentUnit.upgradeDelayMonths - 1
                                if (newDelay <= 0) {
                                    currentUnit = currentUnit.copy(isUpgrading = false, upgradeDelayMonths = 0)
                                } else {
                                    currentUnit = currentUnit.copy(upgradeDelayMonths = newDelay)
                                }
                            }
                            
                            if (!currentUnit.isUpgrading) {
                                if (currentUnit.type == "HOSPITAL") {
                                    val newPatients = (100 * currentUnit.level).toLong()
                                    currentUnit = currentUnit.copy(members = newPatients)
                                    unitRev = newPatients * 5000.0 // Approx $5k per patient / mo
                                    
                                    extraV += (currentUnit.level * 2_000_000L)
                                } else if (currentUnit.type == "INSURANCE") {
                                    val premiumMultiplier = when (currentUnit.tierCategory) {
                                        "PREMIUM" -> 1.5
                                        "ELITE" -> 2.5
                                        else -> 1.0
                                    }
                                    val growth = (100..500).random() * currentUnit.level
                                    currentUnit = currentUnit.copy(members = currentUnit.members + growth)
                                    unitRev = currentUnit.members * 50.0 * premiumMultiplier
                                    
                                    val riskProb = when (currentUnit.tierCategory) {
                                        "PREMIUM" -> 0.08
                                        "ELITE" -> 0.12
                                        else -> 0.05
                                    }
                                    
                                    if (kotlin.random.Random.nextDouble() < riskProb) {
                                        val membersMod = currentUnit.members / 1000.0
                                        val claimDeduction = (1_000_000L..10_000_000L).random().toDouble() * maxOf(1.0, membersMod) * premiumMultiplier
                                        if (currentUnit.unitCash >= claimDeduction) {
                                            currentUnit = currentUnit.copy(unitCash = currentUnit.unitCash - claimDeduction)
                                        } else {
                                            val remainingClaim = claimDeduction - currentUnit.unitCash
                                            currentUnit = currentUnit.copy(unitCash = 0.0)
                                            claimModifiers += remainingClaim
                                        }
                                    }
                                    
                                    extraV += (currentUnit.members * 1_000L) // $1k per member valuation
                                } else if (currentUnit.type == "CLINIC") {
                                    val newPatients = (50 * currentUnit.level).toLong()
                                    currentUnit = currentUnit.copy(members = newPatients)
                                    unitRev = newPatients * 2000.0
                                    extraV += (currentUnit.level * 250_000L)
                                }
                                
                                val maintenance = unitRev * 0.3
                                val netUnitProfit = unitRev - maintenance
                                if (netUnitProfit > 0) {
                                    val dividend = netUnitProfit * 0.2
                                    totalDividendsFromHealthcare += dividend.toLong()
                                    currentUnit = currentUnit.copy(unitCash = currentUnit.unitCash + (netUnitProfit - dividend))
                                } else {
                                    currentUnit = currentUnit.copy(unitCash = maxOf(0.0, currentUnit.unitCash + netUnitProfit))
                                }
                                
                                currentUnit = currentUnit.copy(monthlyRevenue = unitRev)
                            }
                            currentUnit
                        }
                        
                        businessInternalRevenue += totalDividendsFromHealthcare
                        
                        val netProfit = businessInternalRevenue - businessInternalExpenses
                        val (newCompanyCash, dividendToHoldingParent) = processDecentralizedCashFlow(netProfit, owned.companyCash)
                        
                        var finalCompanyCash = newCompanyCash - claimModifiers
                        if (finalCompanyCash < 0) finalCompanyCash = 0.0
                        val updatedTenders = owned.activeTenders.map { tender ->
                            if (!tender.isFinished && tender.remainingMonths > 0) {
                                val newRemaining = tender.remainingMonths - 1
                                if (newRemaining == 0) {
                                    val inject = tender.totalContractValue
                                    finalCompanyCash += inject
                                    tender.copy(remainingMonths = 0, isFinished = true)
                                } else {
                                    tender.copy(remainingMonths = newRemaining)
                                }
                            } else tender
                        }
                        
                        val updatedThemeParkBiddings = processThemeParkBiddingsMonthly(owned.activeThemeParkBiddings)
                        val (updatedThemeParkBranches, parkNetProfit) = processThemeParkMonthly(owned.themeParkBranches, currentState.inGameMonth)
                        if (parkNetProfit > 0) {
                            val retainedEarnings = (parkNetProfit * 0.6).toLong()
                            val divToHolding = parkNetProfit - retainedEarnings
                            finalCompanyCash += retainedEarnings
                            newHoldingCashInflow += divToHolding
                        } else {
                            finalCompanyCash += parkNetProfit
                            if (finalCompanyCash < 0) {
                                newHoldingCashOutflow += Math.abs(finalCompanyCash.toLong())
                                finalCompanyCash = 0.0
                            }
                        }

                        val (updatedHospitalityProperties, hospitalityProfit) = processHospitalityMonthly(owned.hospitalityProperties, currentState.inGameMonth)
                        if (hospitalityProfit > 0) {
                            val retainedEarnings = (hospitalityProfit * 0.6).toLong()
                            val divToHolding = hospitalityProfit - retainedEarnings
                            finalCompanyCash += retainedEarnings
                            newHoldingCashInflow += divToHolding
                        } else {
                            finalCompanyCash += hospitalityProfit
                            if (finalCompanyCash < 0) {
                                newHoldingCashOutflow += Math.abs(finalCompanyCash.toLong())
                                finalCompanyCash = 0.0
                            }
                        }

                        if (dividendToHoldingParent < 0) {
                            newHoldingCashOutflow += (-dividendToHoldingParent)
                        } else {
                            newHoldingCashInflow += dividendToHoldingParent
                        }
                        
                        var newClientProjects = owned.availableClientProjects
                        if (owned.catalogId == "construction") {
                            val count = (1..3).random()
                            val generated = mutableListOf<com.example.data.ConstructionProject>()
                            val types = listOf("Pabrik", "Gedung", "Hotel", "Mall", "Apartemen")
                            for (i in 0 until count) {
                                val baseBudget = (500_000L..5_000_000L).random() * owned.level
                                val dur = (3..12).random()
                                val margin = kotlin.random.Random.nextDouble(0.2, 0.4)
                                val finalProfit = (baseBudget * margin).toLong()
                                generated.add(com.example.data.ConstructionProject(
                                    name = "Klien: ${types.random()}|$baseBudget|$margin",
                                    totalContractValue = finalProfit.toDouble(),
                                    durationMonths = dur,
                                    remainingMonths = dur
                                ))
                            }
                            newClientProjects = generated
                        }
                        
                        var newClientEventRequests = owned.clientEventRequests
                        if (owned.catalogId == "media_radio") {
                            newClientEventRequests = generateEventRequestsForBusiness(owned)
                        }
                        
                        owned.copy(projectHistory = updatedHistory, extraValuation = extraV, companyCash = finalCompanyCash, activeTenders = updatedTenders, subsidiaries = updatedDeepSubs, isUpgrading = isUpgradingNow, upgradeDelayMonths = upgradeDelayNow, availableClientProjects = newClientProjects, healthcareSubsidiaries = updatedHealthcareUnits, clientEventRequests = newClientEventRequests, themeParkBranches = updatedThemeParkBranches, activeThemeParkBiddings = updatedThemeParkBiddings, hospitalityProperties = updatedHospitalityProperties)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        owned
                    }
                }
                
                val netHoldingChange = (newHoldingCashInflow * com.example.data.CorporateFinanceManager.SYNERGY_MULTIPLIER * (holding.ownershipPercentage / 100.0f)).toLong() - (newHoldingCashOutflow * com.example.data.CorporateFinanceManager.SYNERGY_MULTIPLIER * (holding.ownershipPercentage / 100.0f)).toLong()
                
                var finalHoldingCashChange = netHoldingChange.toDouble()
                if (netHoldingChange > 0) {
                    val retainedEarnings = (netHoldingChange * 0.5).toLong()
                    val ceoDividend = netHoldingChange - retainedEarnings
                    finalHoldingCashChange = retainedEarnings.toDouble()
                    monthlyIncome += ceoDividend
                }
                
                val newHoldingCash = if (holding.holdingCash + finalHoldingCashChange < 0) {
                    val shortfall = -(holding.holdingCash + finalHoldingCashChange)
                    monthlyExpenses += shortfall.toLong()
                    0.0
                } else {
                    holding.holdingCash + finalHoldingCashChange
                }
                
                holding.copy(subsidiaries = newSubs, holdingCash = newHoldingCash)
            } catch (e: Exception) {
                e.printStackTrace()
                holding
            }
        }

        val newIpLibraryItems = mutableListOf<com.example.data.TvProgram>()
        var tvProgHasChanges = false
        val rawActiveTvPrograms = if (currentState.activeTvPrograms != null) currentState.activeTvPrograms else emptyList()
        val updatedTvProgs = rawActiveTvPrograms.mapNotNull { prog ->
            try {
                if (prog.active) {
                    tvProgHasChanges = true
                    
                    val fluctuation = 0.85 + Math.random() * 0.30
                    var newRating = (prog.rating ?: 0.0) * fluctuation
                    val maxRating = when(prog.type) {
                        "Hak Siar Olahraga" -> 40.0
                        "Pencarian Bakat (Talent Show)" -> 40.0
                        "Sinetron" -> 35.0
                        "Reality Show" -> 30.0
                        "Hiburan / Musik" -> 25.0
                        "Investigasi Kriminal" -> 25.0
                        "Berita Terkini", "Talkshow" -> 20.0
                        else -> 30.0
                    }
                    newRating = newRating.coerceIn(0.1, maxRating)

                    val marketTrend = 0.7 + Math.random() * 0.7

                    val safeSlots = if (prog.timeSlots != null) prog.timeSlots else emptyList()
                    val timeMuls = safeSlots.map { slot ->
                        val hour = slot.substringBefore(":").toIntOrNull() ?: 12
                        val isHalfHour = slot.substringAfter(":") == "30"
                        val minutes = hour * 60 + (if(isHalfHour) 30 else 0)
                        
                        if (minutes in 6*60 .. 11*60 + 30) 1.0
                        else if (minutes in 12*60 .. 17*60 + 30) 0.6
                        else if (minutes in 18*60 .. 22*60 + 30) 2.5
                        else 0.3
                    }
                    val avgMultiplier = if (timeMuls.isNotEmpty()) timeMuls.average() else 1.0

                    val newAdRev = ((prog.productionCost ?: 0.0) * (newRating / 10.0)) * marketTrend * avgMultiplier

                    val opsPercentage = 0.10 + Math.random() * 0.15
                    val newOpsCost = (prog.productionCost ?: 0.0) * opsPercentage

                    val netIncome = newAdRev - newOpsCost
                    val newProfit = (prog.totalAccumulatedProfit ?: 0.0) + netIncome
                    val newMonths = (prog.monthsAired ?: 0) + 1
                    
                    val updatedProg = prog.copy(
                        previousRating = prog.rating ?: 0.0,
                        rating = newRating,
                        monthlyAdRevenue = newAdRev,
                        currentOperationalCost = newOpsCost,
                        totalAccumulatedProfit = newProfit,
                        monthsAired = newMonths,
                        timeSlots = safeSlots,
                        isOriginalIP = prog.isOriginalIP ?: true
                    )
                    
                    if ((updatedProg.remainingMonths ?: 0) > 0) {
                        val newRem = (updatedProg.remainingMonths ?: 0) - 1
                        if (newRem > 0) {
                            updatedProg.copy(remainingMonths = newRem)
                        } else {
                            // Expired
                            if (updatedProg.isOriginalIP == true) {
                                newIpLibraryItems.add(updatedProg.copy(remainingMonths = 0, active = false))
                            }
                            null
                        }
                    } else {
                        // Manual cancel (internal/original IP)
                        updatedProg
                    }
                } else {
                    prog
                }
            } catch (e: Exception) {
                e.printStackTrace()
                prog
            }
        }
        
        val finalIpLibrary = currentState.ipLibraryHistory + newIpLibraryItems
        val mappedBusinessesWithTvVal = mappedBusinesses

        var appProjHasChanges = false
        var extraIncomeFromApps = 0L
        val synergizedBusinessIds = mutableSetOf<String>()
        val updatedAppProjects = currentState.appProjects.map { proj ->
            if (proj.status == com.example.data.ProjectStatus.DEVELOPMENT) {
                appProjHasChanges = true
                val newMonth = proj.currentMonth + 1
                if (newMonth >= proj.devTimeMonths) {
                    when (proj.type) {
                        com.example.data.ProjectType.CLIENT_B2B -> {
                            extraIncomeFromApps += proj.targetRevenue.toLong()
                            proj.copy(currentMonth = newMonth, status = com.example.data.ProjectStatus.COMPLETED)
                        }
                        com.example.data.ProjectType.INDEPENDENT_SAAS -> {
                            proj.copy(currentMonth = newMonth, status = com.example.data.ProjectStatus.MAINTENANCE)
                        }
                        com.example.data.ProjectType.ECOSYSTEM_SYNERGY -> {
                            if (proj.targetBusinessId != null) {
                                synergizedBusinessIds.add(proj.targetBusinessId)
                            }
                            proj.copy(currentMonth = newMonth, status = com.example.data.ProjectStatus.COMPLETED)
                        }
                    }
                } else {
                    proj.copy(currentMonth = newMonth)
                }
            } else if (proj.status == com.example.data.ProjectStatus.MAINTENANCE && proj.type == com.example.data.ProjectType.INDEPENDENT_SAAS) {
                appProjHasChanges = true
                val fluctuation = 0.8 + Math.random() * 0.4
                val mrr = (proj.targetRevenue * fluctuation).toLong()
                extraIncomeFromApps += mrr
                proj
            } else {
                proj
            }
        }
        
        currentState.appProjects.forEach { proj ->
             if (proj.status == com.example.data.ProjectStatus.DEVELOPMENT) {
                  monthlyExpenses += (proj.budgetCost / proj.devTimeMonths.coerceAtLeast(1)).toLong()
             }
        }
        monthlyIncome += extraIncomeFromApps

        val mappedBusinessesWithAppVal = mappedBusinessesWithTvVal.map { b ->
            if (synergizedBusinessIds.contains(b.instanceId)) {
                b.copy(synergyMultiplier = b.synergyMultiplier + 0.25)
            } else {
                b
            }
        }

        // Real Estate Rental Income goes to Private Cash / Family Office
        var personalRentalIncome = 0L
        currentState.ownedProperties.forEach { owned ->
            val propItem = _realEstateMarket.value.find { it.id == owned.propertyId }
            if (propItem != null) {
                personalRentalIncome += propItem.baseRentalIncome
            }
        }

        var totalPrivateUpkeep = 0L
        currentState.rentedHouses.forEach { rented ->
            totalPrivateUpkeep += rented.monthlyRent
        }

        // Stock Portfolio Dividend Yield (Estimation)
        var totalDividendIncome = 0L
        currentState.ownedStocks.forEach { owned ->
            if (owned.isIntegratedToHolding) return@forEach
            val liveStock = _stockList.value.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val isIndo = owned.ticker.contains(".JK")
                val currentPriceUsd = liveStock.currentPrice
                val stats = com.example.data.getMarketStats(liveStock)
                val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0 // Annual yield / 12 / 100
                val estMonthlyDividend = (owned.shares * currentPriceUsd * monthlyYieldPercent).toLong()
                totalDividendIncome += estMonthlyDividend
            }
        }

        var netProfit = monthlyIncome + totalDividendIncome - monthlyExpenses

        // Apply Mega Holding Logic for Income
        try {
            if (currentState.megaHolding.isActive) {
                var baseMegaIncome = monthlyIncome - monthlyExpenses
                if (currentState.megaHolding.includesInvestments) {
                    baseMegaIncome += totalDividendIncome
                }
                val playerShareIncome = (baseMegaIncome * (currentState.companyOwnershipPercent / 100.0)).toLong()
                
                if (currentState.megaHolding.includesInvestments) {
                    netProfit = playerShareIncome
                } else {
                    netProfit = playerShareIncome + totalDividendIncome
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", "Mega Holding Income calculation error: ${e.message}")
        }
        
        // Game Difficulty Modifications
        val difficulty = _gameDifficulty.value
        if (difficulty == "Hard" && netProfit > 0) {
            netProfit = (netProfit * 0.85).toLong()
        } 

        var inflationDeduction = 0L
        if (difficulty == "Elite Tycoon") {
            inflationDeduction = (currentState.cash * (0.025 / 12.0)).toLong()
        }

        var newMonth = currentState.inGameMonth + 1
        var newYear = currentState.inGameYear
        var finalTaxReport = currentState.taxLegalReport
        var taxDeduction = 0L

        // Tax Calculation based on Total Asset Valuation
        val totalBusinessValuation = mappedBusinesses.sumOf {
            if (it.acquiredStockTicker != null) {
                val stockInPortfolio = currentState.ownedStocks.find { s -> s.ticker == it.acquiredStockTicker }
                val livePrice = _stockList.value.find { s -> s.ticker == it.acquiredStockTicker }?.currentPrice ?: stockInPortfolio?.averagePrice ?: 0.0
                val baseVal = if (stockInPortfolio != null) {
                    (stockInPortfolio.shares * livePrice).toLong()
                } else 0L
                val subsidiariesVal = it.subsidiaries.sumOf { sub ->
                    val catItem = getCatalogItem(sub.catalogId, currentState)
                    if (catItem != null) getBusinessValuation(sub, catItem) else 0L
                }
                baseVal + it.companyCash.toLong() + subsidiariesVal
            } else {
                val catalogItem = getCatalogItem(it.catalogId, currentState)
                if (catalogItem != null) getBusinessValuation(it, catalogItem) else 0L
            }
        }
        val totalPropertyValuation = currentState.ownedProperties.sumOf { it.currentEstimatedValue }
        
        // Separate Corporate / Laba tax and Personal Wealth Tax are now fully separated.
        // Corporate Tax is PPh Badan 20% (or 5% if tax haven active) on net income monthly.
        // Personal Taxes are progressive PPh 21 and PPh Final 10% on realizations.
        // Spt Pelaporan is done annually.
        
        if (newMonth > 12) {
            newMonth = 1
            newYear += 1
            _currentYearStartups.value = com.example.data.generateYearlyStartups(newYear)

            if (finalTaxReport.hasNotary) {
                taxDeduction += 12000L // Annual notary fee
            }
        }
        
        // Unpaid tax penalty and business freezing
        if (finalTaxReport.unpaidTaxes > 0 && !finalTaxReport.hasNotary) {
            finalTaxReport = finalTaxReport.copy(
                unpaidTaxes = (finalTaxReport.unpaidTaxes * 1.05).toLong()
            )
        }
        
        val taxOverdueThreshold = 1000000L
        var currentFrozenBiz = finalTaxReport.frozenBusinessId
        if (finalTaxReport.unpaidTaxes > taxOverdueThreshold) {
            if (currentFrozenBiz == null && mappedBusinesses.isNotEmpty()) {
                currentFrozenBiz = mappedBusinesses.random().instanceId
            }
        } else {
            currentFrozenBiz = null
        }
        finalTaxReport = finalTaxReport.copy(frozenBusinessId = currentFrozenBiz)

        // Random Lawsuit Generation
        var activeLawsuits = finalTaxReport.activeLawsuits
        if (Math.random() < 0.05) { // 5% chance per month
             activeLawsuits = activeLawsuits + com.example.data.generateRandomLawsuit(currentState.netWorth)
        }
        finalTaxReport = finalTaxReport.copy(activeLawsuits = activeLawsuits)
        
        var extraCashFromStartups = 0L
        val remainingStartups = mutableListOf<com.example.data.ActiveStartupInvestment>()
        val newsUpdates = mutableListOf<MarketNews>()
        
        currentState.activeStartupInvestments.forEach { inv ->
            if (inv.monthsRemaining <= 1) {
                if (Math.random() < inv.successProbability) {
                    extraCashFromStartups += inv.potentialReturn
                    newsUpdates.add(
                        MarketNews(
                            id = "startup_win_${System.currentTimeMillis()}_${inv.id}",
                            text = "STARTUP SUKSES: ${inv.startupName} berhasil exit! Anda mendapat profit besar.",
                            type = "BULL"
                        )
                    )
                } else {
                    newsUpdates.add(
                        MarketNews(
                            id = "startup_fail_${System.currentTimeMillis()}_${inv.id}",
                            text = "STARTUP GAGAL: ${inv.startupName} bangkrut. Investasi hangus.",
                            type = "BEAR"
                        )
                    )
                }
            } else {
                remainingStartups.add(inv.copy(monthsRemaining = inv.monthsRemaining - 1))
            }
        }
        
        if (newsUpdates.isNotEmpty()) {
            val combined: List<MarketNews> = newsUpdates + _newsFeed.value
            _newsFeed.value = combined.take(20)
        }
        
        // Subtract tax deduction
        val newCash = currentState.cash + netProfit + extraCashFromStartups - taxDeduction - inflationDeduction
        
        val stocksValue = currentState.ownedStocks.sumOf { owned ->
            if (owned.isIntegratedToHolding) return@sumOf 0L
            val liveStock = _stockList.value.find { it.ticker == owned.ticker }
            val livePrice = liveStock?.currentPrice ?: owned.averagePrice
            (owned.shares * livePrice).toLong()
        }
        val cryptoValue = currentState.ownedCrypto.sumOf { owned ->
            val livePrice = _cryptoList.value.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
            (owned.amount * livePrice).toLong()
        }
        val realEstateValue = currentState.ownedProperties.sumOf { owned ->
            val prop = _realEstateMarket.value.find { it.id == owned.propertyId }
            prop?.basePrice ?: owned.purchasedPrice
        }
        var businessValue = totalBusinessValuation
        val holdingValue = mappedHoldings.sumOf { holding ->
            val subVal = holding.subsidiaries.sumOf { sub ->
                if (sub.acquiredStockTicker != null) {
                    val stockInPortfolio = currentState.ownedStocks.find { s -> s.ticker == sub.acquiredStockTicker }
                    val livePrice = _stockList.value.find { s -> s.ticker == sub.acquiredStockTicker }?.currentPrice ?: stockInPortfolio?.averagePrice ?: 0.0
                    val baseVal = if (stockInPortfolio != null) {
                        (stockInPortfolio.shares * livePrice).toLong()
                    } else 0L
                    val subsidiariesVal = sub.subsidiaries.sumOf { s ->
                        val catItem = getCatalogItem(s.catalogId, currentState)
                        if (catItem != null) getBusinessValuation(s, catItem) else 0L
                    }
                    baseVal + sub.companyCash.toLong() + subsidiariesVal
                } else {
                    val catalogItem = getCatalogItem(sub.catalogId, currentState)
                    if (catalogItem != null) getBusinessValuation(sub, catalogItem) else 0L
                }
            }
            subVal
        }
        businessValue += holdingValue
        
        val collectionValue = currentState.ownedCollections.sumOf { it.purchasedPrice }
        
        // Handle Time Deposits
        var maturedDepositReturn = 0L
        val remainingDeposits = mutableListOf<com.example.data.TimeDeposit>()
        currentState.timeDeposits.forEach { deposit ->
            if (deposit.monthsRemaining <= 1) {
                // Mature! Return principal + interest
                maturedDepositReturn += (deposit.principal + (deposit.principal * deposit.interestRate)).toLong()
            } else {
                remainingDeposits.add(deposit.copy(monthsRemaining = deposit.monthsRemaining - 1))
            }
        }
        val finalCashAfterDeposits = newCash

        // Calculate Global Stock Market Bear Phase Ratio
        val totalStocks = stockTrends.size
        val bearCount = stockTrends.values.count { it.currentTrend == MarketTrend.BEAR_MARKET || it.currentTrend == MarketTrend.STEADY_BLEED || it.currentTrend == MarketTrend.THE_LOST_DECADE }
        val bearRatio = if (totalStocks > 0) bearCount.toDouble() / totalStocks else 0.0

        // Update Metal Prices (Safe Haven)
        val currentMetals = _preciousMetalsList.value.map { metal ->
            // If bear ratio > 0.5, commodities go up significantly. If bull ratio > 0.5, they stagnate or slowly drop.
            val bearShift = (bearRatio - 0.5) * 0.15 // e.g. bear=0.8 -> shift = +0.045
            val randVol = (Math.random() - 0.4) * 0.05 * _marketVolatilityFactor.value // slightly upward biased random
            val totalShift = bearShift + randVol
            val newPrice = metal.currentPrice * (1.0 + totalShift)
            metal.copy(currentPrice = newPrice.coerceAtLeast(0.1))
        }
        _preciousMetalsList.value = currentMetals

        val metalsValue = currentState.ownedMetals.entries.sumOf { (id, amount) ->
            val livePrice = currentMetals.find { it.id == id }?.currentPrice ?: 0.0
            (amount * livePrice).toLong()
        }
        
        val housingValue = currentState.ownedHouses.sumOf { it.purchasedPrice }
        
        val depositsValue = remainingDeposits.sumOf { it.principal }

        val newNetWorth = try {
            if (currentState.megaHolding.isActive) {
                var baseMegaValuation = businessValue
                val otherAssetsVal = cryptoValue + realEstateValue + collectionValue + metalsValue + housingValue + depositsValue + finalCashAfterDeposits
                
                if (currentState.megaHolding.includesInvestments) {
                    baseMegaValuation += stocksValue
                    (baseMegaValuation * (currentState.companyOwnershipPercent / 100.0)).toLong() + otherAssetsVal
                } else {
                    (baseMegaValuation * (currentState.companyOwnershipPercent / 100.0)).toLong() + stocksValue + otherAssetsVal
                }
            } else {
                finalCashAfterDeposits + stocksValue + cryptoValue + realEstateValue + businessValue + collectionValue + metalsValue + housingValue + depositsValue
            }
        } catch (e: Exception) {
            android.util.Log.e("GameViewModel", "Mega Holding Valuation error: ${e.message}")
            finalCashAfterDeposits + stocksValue + cryptoValue + realEstateValue + businessValue + collectionValue + metalsValue + housingValue + depositsValue
        }

        var familyOfficeCash = finalCashAfterDeposits
        var familyOfficeHoldings = mappedHoldings
        var familyOfficeBusinesses = mappedBusinessesWithAppVal
        var familyOfficeProperties = currentState.ownedProperties
        var familyOfficeHouses = currentState.ownedHouses
        var familyOfficeCollections = currentState.ownedCollections
        var familyOfficeDebt = currentState.personalDebt

        val monthlyLedgerRecords = mutableListOf<com.example.data.PrivateLedgerRecord>()
        val foNews = mutableListOf<MarketNews>()
        if (maturedDepositReturn > 0) {
            foNews.add(MarketNews(
                id = "deposit_mature_${System.currentTimeMillis()}",
                text = "DEPOSITO JATUH TEMPO: Deposito berjangka Anda telah jatuh tempo! Dana sebesar $${com.example.ui.formatCurrencyRingkas(maturedDepositReturn.toDouble(), false)} telah dicairkan dan masuk ke Kas Pribadi (Family Office).",
                type = "BULL"
            ))
            monthlyLedgerRecords.add(
                com.example.data.PrivateLedgerRecord(
                    monthTick = newMonth,
                    title = "Deposito Jatuh Tempo beserta Bunga",
                    amount = maturedDepositReturn,
                    isIncome = true
                )
            )
        }

        val megaHoldingMonthlyProfit = currentState.ownedBusinesses.sumOf {
            val ct = getCatalogItem(it.catalogId, currentState)
            if (ct != null) getBusinessStats(it, ct, currentState).let { (rev, mnt) -> rev - mnt } else 0L
        } + currentState.holdingCompanies.sumOf { h ->
            h.subsidiaries.sumOf { sub ->
                val ct = getCatalogItem(sub.catalogId, currentState)
                if (ct != null) getBusinessStats(sub, ct, currentState).let { (rev, mnt) -> rev - mnt } else 0L
            }
        }

        // ----------------------------------------------------
        // BOARD APPROVAL LOGICAL HANDLING
        // ----------------------------------------------------
        var currentCeoSalaryPercentVal = currentState.currentCeoSalaryPercent
        var pendingCeoSalaryPercentVal = currentState.pendingCeoSalaryPercent
        var boardApprovalMonthsLeftVal = currentState.boardApprovalMonthsLeft
        var lastSalaryRequestMonthVal = currentState.lastSalaryRequestMonth
        var boardReplyMessageVal = currentState.boardReplyMessage

        var currentDividendPercentVal = currentState.currentDividendPercent
        var pendingDividendPercentVal = currentState.pendingDividendPercent
        var dividendApprovalMonthsLeftVal = currentState.dividendApprovalMonthsLeft
        var lastDividendRequestMonthVal = currentState.lastDividendRequestMonth
        var currentTantiemPercentVal = currentState.currentTantiemPercent
        var pendingTantiemPercentVal = currentState.pendingTantiemPercent
        var tantiemApprovalMonthsLeftVal = currentState.tantiemApprovalMonthsLeft
        var familyOfficePrivateBalanceVal = currentState.privateBalance + maturedDepositReturn + personalRentalIncome
        
        // Log rental income to ledger
        if (personalRentalIncome > 0) {
            monthlyLedgerRecords.add(
                com.example.data.PrivateLedgerRecord(
                    monthTick = newMonth,
                    title = "Pendapatan Sewa Properti Pribadi",
                    amount = personalRentalIncome,
                    isIncome = true
                )
            )
        }

        // A. Hitung & Cairkan Dividen Saham Pribadi (Passive Income)
        var totalPrivateStockDividend = 0.0
        currentState.privateStockPortfolio.forEach { owned ->
            val liveStock = _stockList.value.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val stats = com.example.data.getMarketStats(liveStock)
                val livePrice = liveStock.currentPrice
                val portfolioValue = owned.shares * livePrice
                val monthlyStockDividend = (portfolioValue * (stats.dividendYield / 100.0)) / 12.0
                totalPrivateStockDividend += monthlyStockDividend
            }
        }
        val roundedPrivateStockDividend = totalPrivateStockDividend.toLong()
        if (roundedPrivateStockDividend > 0) {
            familyOfficePrivateBalanceVal += roundedPrivateStockDividend
            monthlyLedgerRecords.add(
                com.example.data.PrivateLedgerRecord(
                    monthTick = newMonth,
                    title = "Dividen Saham Pribadi (Passive Income)",
                    amount = roundedPrivateStockDividend,
                    isIncome = true
                )
            )
        }

        var retainedEarningsVal = currentState.retainedEarnings
        var financialHistoryVal = currentState.financialHistory
        
        var totalTaxPaidVal = currentState.totalTaxPaid
        var corporateTaxPaidVal = currentState.corporateTaxPaid
        var personalTaxPaidVal = currentState.personalTaxPaid
        var isSptReportedThisYearVal = currentState.isSptReportedThisYear
        var consecutiveUnreportedSptVal = currentState.consecutiveUnreportedSpt

        if (boardApprovalMonthsLeftVal > 0) {
            boardApprovalMonthsLeftVal -= 1
        }
        if (tantiemApprovalMonthsLeftVal > 0) {
            tantiemApprovalMonthsLeftVal -= 1
        }

        if (boardApprovalMonthsLeftVal == 0 && pendingCeoSalaryPercentVal != null) {
            val proposedJump = pendingCeoSalaryPercentVal - currentCeoSalaryPercentVal
            val isProfitable = megaHoldingMonthlyProfit > 0
            var isApproved = false

            if (!isProfitable && proposedJump > 0) {
                boardReplyMessageVal = "DITOLAK: Perusahaan sedang merugi! Dewan menolak kenaikan gaji."
            } else if (proposedJump > 2.0) {
                if ((1..100).random() <= 10) {
                    isApproved = true
                } else {
                    boardReplyMessageVal = "DITOLAK: Kenaikan terlalu drastis! Dewan menyarankan kenaikan bertahap."
                }
            } else if (proposedJump > 1.0) {
                if ((1..100).random() <= 50) {
                    isApproved = true
                } else {
                    boardReplyMessageVal = "DITOLAK: Kinerja saat ini belum menjustifikasi kenaikan tersebut."
                }
            } else {
                if ((1..100).random() <= 95) {
                    isApproved = true
                } else {
                    boardReplyMessageVal = "DITOLAK: Dewan menunda kenaikan untuk menjaga arus kas."
                }
            }

            if (proposedJump < 0.0) {
                isApproved = true
                boardReplyMessageVal = "DISETUJUI: Dewan mengapresiasi pemotongan gaji untuk efisiensi."
            }

            if (isApproved) {
                currentCeoSalaryPercentVal = pendingCeoSalaryPercentVal
                boardReplyMessageVal = "DISETUJUI: RUPS menyetujui remunerasi baru sebesar ${currentCeoSalaryPercentVal}%."
            }

            lastSalaryRequestMonthVal = currentState.inGameMonth
            pendingCeoSalaryPercentVal = null
        }

        // Dividends Board Review
        if (dividendApprovalMonthsLeftVal > 0) {
            dividendApprovalMonthsLeftVal -= 1
        }

        if (dividendApprovalMonthsLeftVal == 0 && pendingDividendPercentVal != null) {
            val proposedVal = pendingDividendPercentVal
            val diff = proposedVal - currentDividendPercentVal
            val isProfitable = megaHoldingMonthlyProfit > 0
            var isApproved = false
            var boardReply = ""

            if (!isProfitable && diff > 0) {
                boardReply = "DITOLAK: Perusahaan sedang merugi! Dewan menolak menaikkan persentase dividen."
            } else if (proposedVal > 30.0) {
                if ((1..100).random() <= 5) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Kebijakan dividen terlalu agresif (>30%)! Dewan menolaknya demi menjaga ketahanan finansial."
                }
            } else if (proposedVal > 15.0) {
                if ((1..100).random() <= 40) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Dewan menangguhkan rencana dividen moderat (${proposedVal}%) demi ekspansi strategis."
                }
            } else {
                if ((1..100).random() <= 90) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Dewan memutuskan menahan cadangan laba lebih tinggi."
                }
            }

            if (diff < 0.0) {
                isApproved = true
                boardReply = "DISETUJUI: Dewan mengapresiasi keputusan untuk meningkatkan laba ditahan."
            }

            if (isApproved) {
                currentDividendPercentVal = proposedVal
                boardReplyMessageVal = "DISETUJUI: Kebijakan dividen semi-tahunan baru sebesar ${currentDividendPercentVal}% disetujui RUPS."
            } else {
                boardReplyMessageVal = boardReply
            }

            lastDividendRequestMonthVal = currentState.inGameMonth
            pendingDividendPercentVal = null
        }

        if (tantiemApprovalMonthsLeftVal == 0 && pendingTantiemPercentVal != null) {
            val proposedVal = pendingTantiemPercentVal
            val diff = proposedVal - currentTantiemPercentVal
            val isProfitable = megaHoldingMonthlyProfit > 0
            var isApproved = false
            var boardReply = ""

            if (!isProfitable && diff > 0) {
                boardReply = "DITOLAK: Perusahaan sedang merugi! Dewan menolak menetapkan bonus tahunan."
            } else if (proposedVal > 10.0) {
                if ((1..100).random() <= 8) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Permintaan tantiem terlalu tinggi (>10%)! Dewan menolaknya demi menjaga kepentingan pemegang saham publik."
                }
            } else if (proposedVal > 5.0) {
                if ((1..100).random() <= 45) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Dewan merasa persentase tantiem moderat (${proposedVal}%) memerlukan pembuktian kinerja finansial lebih lanjut."
                }
            } else {
                if ((1..100).random() <= 85) {
                    isApproved = true
                } else {
                    boardReply = "DITOLAK: Dewan menunda persetujuan remunerasi bonus tahunan."
                }
            }

            if (diff < 0.0) {
                isApproved = true
                boardReply = "DISETUJUI: Dewan menyetujui efisiensi remunerasi bonus tahunan."
            }

            if (isApproved) {
                currentTantiemPercentVal = proposedVal
                boardReplyMessageVal = "DISETUJUI: RUPS menyetujui pengajuan tantiem tahunan sebesar ${currentTantiemPercentVal}% dari total Laba Bersih Tahunan."
            } else {
                boardReplyMessageVal = boardReply
            }

            pendingTantiemPercentVal = null
        }

        // 1. Executive Remuneration (CEO Salary)
        var actualCeoSalaryPaid = 0L
        val ceoSalaryPercentVal = currentCeoSalaryPercentVal
        if (ceoSalaryPercentVal > 0.0 && megaHoldingMonthlyProfit > 0) {
            val nominalCeoSalary = (megaHoldingMonthlyProfit * (ceoSalaryPercentVal / 100.0)).toLong()
            if (nominalCeoSalary > 0) {
                val holdingCashAvailable = familyOfficeHoldings.sumOf { it.holdingCash } + familyOfficeBusinesses.sumOf { it.companyCash }
                val amountToTransfer = Math.min(nominalCeoSalary.toDouble(), holdingCashAvailable)
                if (amountToTransfer > 0) {
                    actualCeoSalaryPaid = amountToTransfer.toLong()
                    var remainingDeduct = amountToTransfer
                    familyOfficeHoldings = familyOfficeHoldings.map { h ->
                        if (remainingDeduct <= 0.0) h
                        else {
                            val v = h.holdingCash
                            val d = Math.min(v, remainingDeduct)
                            remainingDeduct -= d
                            h.copy(holdingCash = v - d)
                        }
                    }
                    familyOfficeBusinesses = familyOfficeBusinesses.map { b ->
                        if (remainingDeduct <= 0.0) b
                        else {
                            val v = b.companyCash
                            val d = Math.min(v, remainingDeduct)
                            remainingDeduct -= d
                            b.copy(companyCash = v - d)
                        }
                    }
                    foNews.add(MarketNews(
                        id = "fo_salary_${System.currentTimeMillis()}",
                        text = "FAMILY OFFICE: Gaji CEO $${com.example.ui.formatCurrencyRingkas(amountToTransfer, false)} (${ceoSalaryPercentVal}%) ditarik kotor dari Kas Holding.",
                        type = "BULL"
                    ))
                }
            }
        }

        // Corporate Perks
        var actualPerksPaid = 0L
        val monthlyPerksValue = (businessValue * 0.000005).toLong()
        if (monthlyPerksValue > 0) {
            val holdingCashAvailable = familyOfficeHoldings.sumOf { it.holdingCash } + familyOfficeBusinesses.sumOf { it.companyCash }
            val amountToTransfer = Math.min(monthlyPerksValue.toDouble(), holdingCashAvailable)
            if (amountToTransfer > 0) {
                actualPerksPaid = amountToTransfer.toLong()
                var remainingDeduct = amountToTransfer
                familyOfficeHoldings = familyOfficeHoldings.map { h ->
                    if (remainingDeduct <= 0.0) h
                    else {
                        val v = h.holdingCash
                        val d = Math.min(v, remainingDeduct)
                        remainingDeduct -= d
                        h.copy(holdingCash = v - d)
                    }
                }
                familyOfficeBusinesses = familyOfficeBusinesses.map { b ->
                    if (remainingDeduct <= 0.0) b
                    else {
                        val v = b.companyCash
                        val d = Math.min(v, remainingDeduct)
                        remainingDeduct -= d
                        b.copy(companyCash = v - d)
                    }
                }
                
                foNews.add(MarketNews(
                    id = "fo_perks_${System.currentTimeMillis()}",
                    text = "CORPORATE PERKS: Tunjangan Natura CEO kotor sebesar $${com.example.ui.formatCurrencyRingkas(actualPerksPaid.toDouble(), false)} dialokasikan dari Kas Holding.",
                    type = "BULL"
                ))
            }
        }

        // Calculate and apply progressive PPh 21 tax for Ceo Salary & perks
        val grossExecutiveIncome = actualCeoSalaryPaid + actualPerksPaid
        if (grossExecutiveIncome > 0) {
            val pph21Tax = calculateProgressiveTax(grossExecutiveIncome, currentState.privateTaxServiceLevel)
            val netExecutiveIncome = grossExecutiveIncome - pph21Tax
            familyOfficePrivateBalanceVal += netExecutiveIncome
            personalTaxPaidVal += pph21Tax
            totalTaxPaidVal += pph21Tax
            
            monthlyLedgerRecords.add(
                com.example.data.PrivateLedgerRecord(
                    monthTick = newMonth,
                    title = "Gaji & Tunjangan CEO kotor",
                    amount = grossExecutiveIncome,
                    isIncome = true
                )
            )
            if (pph21Tax > 0) {
                monthlyLedgerRecords.add(
                    com.example.data.PrivateLedgerRecord(
                        monthTick = newMonth,
                        title = "Pajak PPh 21 Progresif",
                        amount = pph21Tax,
                        isIncome = false
                    )
                )
            }
            
            val effectiveRate = if (grossExecutiveIncome > 0) (pph21Tax.toDouble() / grossExecutiveIncome.toDouble() * 100).toInt() else 0
            foNews.add(MarketNews(
                id = "fo_pph21_${System.currentTimeMillis()}",
                text = "PAJAK PPh 21: Penghasilan eksekutif kotor $${com.example.ui.formatCurrencyRingkas(grossExecutiveIncome.toDouble(), false)} dikenakan PPh 21 Progresif senilai $${com.example.ui.formatCurrencyRingkas(pph21Tax.toDouble(), false)} (Efektif ~${effectiveRate}%). Bersih $${com.example.ui.formatCurrencyRingkas(netExecutiveIncome.toDouble(), false)} disalurkan ke Kas Pribadi.",
                type = "NEUTRAL"
            ))
        }

        // Old Semi-Annual Dividend Distribution Cycle was removed from here to be processed after monthly financial closure (when current month's financial records are complete)

        // --- PRIVATE LIFESTYLE UPKEEP & MAINTENANCE SINKS ---
        currentState.ownedHouses.forEach { owned ->
            totalPrivateUpkeep += (owned.purchasedPrice * 0.002).toLong()
        }
        currentState.ownedCollections.forEach { owned ->
            totalPrivateUpkeep += (owned.purchasedPrice * 0.001).toLong()
        }

        if (totalPrivateUpkeep > 0) {
            if (familyOfficePrivateBalanceVal >= totalPrivateUpkeep) {
                familyOfficePrivateBalanceVal -= totalPrivateUpkeep
                foNews.add(MarketNews(
                    id = "fo_lifestyle_upkeep_${System.currentTimeMillis()}",
                    text = "LIFESTYLE UPKEEP: Membayar sewa, pajak properti & biaya perawatan aset pribadi sebesar $${com.example.ui.formatCurrencyRingkas(totalPrivateUpkeep.toDouble(), false)} dari Kas Pribadi.",
                    type = "BEAR"
                ))
                monthlyLedgerRecords.add(
                    com.example.data.PrivateLedgerRecord(
                        monthTick = newMonth,
                        title = "Lifestyle Upkeep & Perawatan Aset",
                        amount = totalPrivateUpkeep,
                        isIncome = false
                    )
                )
            } else {
                val deficit = totalPrivateUpkeep - familyOfficePrivateBalanceVal
                familyOfficePrivateBalanceVal = 0L
                var remainingDeficit = deficit
                
                // Liquidation sequence
                if (familyOfficeCollections.isNotEmpty()) {
                    val toSell = familyOfficeCollections.first()
                    familyOfficeCollections = familyOfficeCollections.drop(1)
                    val liquidationValue = (toSell.purchasedPrice * 0.7).toLong() // 30% discount
                    remainingDeficit -= liquidationValue
                    familyOfficePrivateBalanceVal += liquidationValue
                    foNews.add(MarketNews(
                        id = "fo_lique_collection_${System.currentTimeMillis()}",
                        text = "LIKUIDASI DARURAT: Koleksi/kendaraan Anda dilikuidasi seharga $${com.example.ui.formatCurrencyRingkas(liquidationValue.toDouble(), false)} untuk menutupi biaya hidup pribadi.",
                        type = "BEAR"
                    ))
                }
                if (remainingDeficit > 0 && familyOfficeHouses.isNotEmpty()) {
                    val toSell = familyOfficeHouses.first()
                    familyOfficeHouses = familyOfficeHouses.drop(1)
                    val liquidationValue = (toSell.purchasedPrice * 0.7).toLong()
                    remainingDeficit -= liquidationValue
                    familyOfficePrivateBalanceVal += liquidationValue
                    foNews.add(MarketNews(
                        id = "fo_lique_house_${System.currentTimeMillis()}",
                        text = "LIKUIDASI DARURAT: Rumah tinggal (ID ${toSell.housingId}) disita & dilikuidasi seharga $${com.example.ui.formatCurrencyRingkas(liquidationValue.toDouble(), false)} untuk menutupi tunggakan sewa/perawatan.",
                        type = "BEAR"
                    ))
                }
                
                if (familyOfficePrivateBalanceVal >= remainingDeficit) {
                    familyOfficePrivateBalanceVal -= remainingDeficit
                    remainingDeficit = 0
                } else {
                    remainingDeficit -= familyOfficePrivateBalanceVal
                    familyOfficePrivateBalanceVal = 0
                }

                if (remainingDeficit > 0) {
                    val debtPenalty = (remainingDeficit * 1.05).toLong()
                    familyOfficeDebt += debtPenalty
                    foNews.add(MarketNews(
                        id = "fo_deficit_debt_${System.currentTimeMillis()}",
                        text = "TUNGGAKAN BIAYA HIDUP: Kas Pribadi minus! Sisa kewajiban sewa & perawatan sebesar $${com.example.ui.formatCurrencyRingkas(remainingDeficit.toDouble(), false)} dialihkan menjadi utang bersuku bunga denda.",
                        type = "BEAR"
                    ))
                }
            }
        }

        // 2. Lombard Loan Interest
        if (familyOfficeDebt > 0) {
            val interestBunga = (familyOfficeDebt * 0.05 / 12.0).toLong()
            if (interestBunga > 0) {
                if (familyOfficePrivateBalanceVal >= interestBunga) {
                    familyOfficePrivateBalanceVal -= interestBunga
                    foNews.add(MarketNews(
                        id = "fo_interest_${System.currentTimeMillis()}",
                        text = "FAMILY OFFICE: Membayar bunga pinjaman Lombard $${com.example.ui.formatCurrencyRingkas(interestBunga, false)} dari Kas Pribadi.",
                        type = "BEAR"
                    ))
                } else {
                    if (familyOfficeProperties.isNotEmpty()) {
                        val seized = familyOfficeProperties.first()
                        familyOfficeProperties = familyOfficeProperties.drop(1)
                        val debtReduction = (seized.currentEstimatedValue * 0.8).toLong()
                        familyOfficeDebt = (familyOfficeDebt - debtReduction).coerceAtLeast(0L)
                        foNews.add(MarketNews(
                            id = "fo_seizure_${System.currentTimeMillis()}",
                            text = "KOLATERAL DISITA: Bank menyita Real estate ID ${seized.propertyId} karena Kas Pribadi kurang bayar bunga. Utang berkurang $${com.example.ui.formatCurrencyRingkas(debtReduction.toDouble(), false)}.",
                            type = "BEAR"
                        ))
                    } else if (familyOfficeHouses.isNotEmpty()) {
                        val seized = familyOfficeHouses.first()
                        familyOfficeHouses = familyOfficeHouses.drop(1)
                        val debtReduction = (seized.purchasedPrice * 0.8).toLong()
                        familyOfficeDebt = (familyOfficeDebt - debtReduction).coerceAtLeast(0L)
                        foNews.add(MarketNews(
                            id = "fo_seizure_${System.currentTimeMillis()}",
                            text = "KOLATERAL DISITA: Bank menyita Rumah tinggal (ID ${seized.housingId}) karena Kas Pribadi kurang bayar bunga. Utang berkurang $${com.example.ui.formatCurrencyRingkas(debtReduction.toDouble(), false)}.",
                            type = "BEAR"
                        ))
                    } else if (familyOfficeCollections.isNotEmpty()) {
                        val seized = familyOfficeCollections.first()
                        familyOfficeCollections = familyOfficeCollections.drop(1)
                        val debtReduction = (seized.purchasedPrice * 0.8).toLong()
                        familyOfficeDebt = (familyOfficeDebt - debtReduction).coerceAtLeast(0L)
                        foNews.add(MarketNews(
                            id = "fo_seizure_${System.currentTimeMillis()}",
                            text = "KOLATERAL DISITA: Bank menyita koleksi atau kendaraan Anda: ${seized.itemId ?: "Aset Koleksi"} karena Kas Pribadi kurang bayar bunga. Utang berkurang $${com.example.ui.formatCurrencyRingkas(debtReduction.toDouble(), false)}.",
                            type = "BEAR"
                        ))
                    } else {
                        val penaltyDebt = (familyOfficeDebt * 1.05).toLong()
                        familyOfficeDebt = penaltyDebt
                        foNews.add(MarketNews(
                            id = "fo_penalty_${System.currentTimeMillis()}",
                            text = "LOMBARD PENALTY: Pinjaman Lombard gagal bayar bunga & tidak ada kolateral fisik disita! Bunga denda 5% ditambahkan ke utang.",
                            type = "BEAR"
                        ))
                    }
                    familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - interestBunga).coerceAtLeast(0L)
                }
            }
        }

        // 1b. Lifestyle Auto-Billing
        val lifestyleCost = currentState.monthlyLifestyleCost
        if (lifestyleCost > 0) {
            familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - lifestyleCost).coerceAtLeast(0L)
            monthlyLedgerRecords.add(
                com.example.data.PrivateLedgerRecord(
                    monthTick = newMonth,
                    title = "Tagihan Gaya Hidup & Keamanan",
                    amount = lifestyleCost,
                    isIncome = false
                )
            )
        }

        var totalRevenueSemuaDivisi = 0L
        var totalExpenseSemuaDivisi = 0L

        familyOfficeBusinesses.forEach {
            val ct = getCatalogItem(it.catalogId, currentState)
            if (ct != null) {
                val (rev, mnt) = getBusinessStats(it, ct, currentState)
                totalRevenueSemuaDivisi += rev
                totalExpenseSemuaDivisi += mnt
            }
        }
        familyOfficeHoldings.forEach { h ->
            h.subsidiaries.forEach { sub ->
                val ct = getCatalogItem(sub.catalogId, currentState)
                if (ct != null) {
                    val (rev, mnt) = getBusinessStats(sub, ct, currentState)
                    totalRevenueSemuaDivisi += rev
                    totalExpenseSemuaDivisi += mnt
                }
            }
        }

        val holdingNetIncome = totalRevenueSemuaDivisi - totalExpenseSemuaDivisi - actualCeoSalaryPaid - actualPerksPaid
        
        // Apply Corporate Tax (20% or 5% if tax haven active) on net income before Retained Earnings
        val corpTaxRate = if (finalTaxReport.isTaxHavenActive) 0.05 else 0.20
        var monthlyCorpTax = 0L
        if (holdingNetIncome > 0) {
            monthlyCorpTax = (holdingNetIncome * corpTaxRate).toLong()
            corporateTaxPaidVal += monthlyCorpTax
        }
        val holdingNetIncomeAfterTax = holdingNetIncome - monthlyCorpTax
        retainedEarningsVal += holdingNetIncomeAfterTax

        if (monthlyCorpTax > 0) {
            foNews.add(MarketNews(
                id = "corporate_tax_paid_${System.currentTimeMillis()}",
                text = "PAJAK BADAN: Laba bersih Mega Holding kotor sebesar $${com.example.ui.formatCurrencyRingkas(holdingNetIncome.toDouble(), false)} dipotong PPh Badan ${if (finalTaxReport.isTaxHavenActive) "5% (Offshore Tax Haven)" else "20%"} senilai $${com.example.ui.formatCurrencyRingkas(monthlyCorpTax.toDouble(), false)}. Laba bersih setelah pajak $${com.example.ui.formatCurrencyRingkas(holdingNetIncomeAfterTax.toDouble(), false)} dialokasikan ke Laba Ditahan.",
                type = "NEUTRAL"
            ))
        }

        val newRecord = com.example.data.MonthlyFinancialRecord(
            monthTick = newMonth,
            totalRevenue = totalRevenueSemuaDivisi,
            totalExpense = totalExpenseSemuaDivisi,
            netIncome = holdingNetIncome
        )

        financialHistoryVal = (financialHistoryVal + newRecord).let {
            if (it.size > 120) it.drop(it.size - 120) else it
        }

        // Semi-Annual Dividend Distribution Cycle (Based on 6-Month Period Net Income)
        if (newMonth % 6 == 0 && currentDividendPercentVal > 0.0) {
            val labaEnamBulanTerakhir = financialHistoryVal.takeLast(6).sumOf { it.netIncome }
            if (labaEnamBulanTerakhir > 0) {
                val proposedDividendPool = (labaEnamBulanTerakhir * (currentDividendPercentVal / 100.0)).toLong()
                val holdingTreasuryCash = familyOfficeHoldings.sumOf { it.holdingCash } + familyOfficeBusinesses.sumOf { it.companyCash }
                // Cap by both retainedEarningsVal and holdingTreasuryCash to prevent negative values or cash overdraw
                val dividendPool = Math.min(proposedDividendPool, Math.min(retainedEarningsVal, holdingTreasuryCash.toLong())).coerceAtLeast(0L)
                
                if (dividendPool > 0) {
                    retainedEarningsVal = (retainedEarningsVal - dividendPool).coerceAtLeast(0L)
                    val playerDividend = dividendPool * (currentState.companyOwnershipPercent / 100.0)
                    
                    var remainingDeduct = dividendPool.toDouble()
                    
                    familyOfficeHoldings = familyOfficeHoldings.map { h ->
                        if (remainingDeduct <= 0.0) h
                        else {
                            val v = h.holdingCash
                            val d = Math.min(v, remainingDeduct)
                            remainingDeduct -= d
                            h.copy(holdingCash = v - d)
                        }
                    }
                    familyOfficeBusinesses = familyOfficeBusinesses.map { b ->
                        if (remainingDeduct <= 0.0) b
                        else {
                            val v = b.companyCash
                            val d = Math.min(v, remainingDeduct)
                            remainingDeduct -= d
                            b.copy(companyCash = v - d)
                        }
                    }
                    
                    val grossDividend = playerDividend.toLong()
                    val dividendTaxRate = if (currentState.privateTaxServiceLevel == 2) 0.05 else 0.10
                    val dividendTax = (grossDividend * dividendTaxRate).toLong()
                    val netDividend = grossDividend - dividendTax
                    
                    familyOfficePrivateBalanceVal += netDividend
                    personalTaxPaidVal += dividendTax
                    totalTaxPaidVal += dividendTax
                    
                    monthlyLedgerRecords.add(
                        com.example.data.PrivateLedgerRecord(
                            monthTick = newMonth,
                            title = "Dividen Holding kotor (${currentDividendPercentVal}%)",
                            amount = grossDividend,
                            isIncome = true
                        )
                    )
                    if (dividendTax > 0) {
                        monthlyLedgerRecords.add(
                            com.example.data.PrivateLedgerRecord(
                                monthTick = newMonth,
                                title = "PPh Final Dividen Holding",
                                amount = dividendTax,
                                isIncome = false
                            )
                        )
                    }
                    
                    val finalDivRateText = if (currentState.privateTaxServiceLevel == 2) "5% (Tax Lawyer Disc)" else "10%"
                    foNews.add(MarketNews(
                        id = "fo_dividend_cycle_${System.currentTimeMillis()}",
                        text = "DIVIDEN TENGAH TAHUNAN: RUPS mencairkan dividen ${currentDividendPercentVal}% dari Laba Bersih 6 Bulan Terakhir ($${com.example.ui.formatCurrencyRingkas(labaEnamBulanTerakhir.toDouble(), false)}). Total pool dividen: $${com.example.ui.formatCurrencyRingkas(dividendPool.toDouble(), false)}. Bagian kotor Anda $${com.example.ui.formatCurrencyRingkas(grossDividend.toDouble(), false)}, dipotong PPh Final ${finalDivRateText} ($${com.example.ui.formatCurrencyRingkas(dividendTax.toDouble(), false)}). Bersih $${com.example.ui.formatCurrencyRingkas(netDividend.toDouble(), false)} ditambahkan ke Kas Pribadi.",
                        type = "BULL"
                    ))
                }
            }
        }

        // B. Pencairan Tantiem (Tahunan)
        var actualTantiemPaid = 0L
        if (newMonth % 12 == 0 && currentTantiemPercentVal > 0.0) {
            val annualNetIncome = financialHistoryVal.takeLast(12).sumOf { it.netIncome }
            if (annualNetIncome > 0) {
                val tantiemBonus = (annualNetIncome * (currentTantiemPercentVal / 100.0)).toLong()
                if (tantiemBonus > 0) {
                    val holdingCashAvailable = familyOfficeHoldings.sumOf { it.holdingCash } + familyOfficeBusinesses.sumOf { it.companyCash }
                    val amountToTransfer = Math.min(tantiemBonus.toDouble(), holdingCashAvailable)
                    if (amountToTransfer > 0) {
                        actualTantiemPaid = amountToTransfer.toLong()
                        var remainingDeduct = amountToTransfer
                        familyOfficeHoldings = familyOfficeHoldings.map { h ->
                            if (remainingDeduct <= 0.0) h
                            else {
                                val v = h.holdingCash
                                val d = Math.min(v, remainingDeduct)
                                remainingDeduct -= d
                                h.copy(holdingCash = v - d)
                            }
                        }
                        familyOfficeBusinesses = familyOfficeBusinesses.map { b ->
                            if (remainingDeduct <= 0.0) b
                            else {
                                val v = b.companyCash
                                val d = Math.min(v, remainingDeduct)
                                remainingDeduct -= d
                                b.copy(companyCash = v - d)
                            }
                        }
                        
                        val grossTantiem = actualTantiemPaid
                        val tantiemTaxRate = if (currentState.privateTaxServiceLevel == 2) 0.05 else 0.10
                        val tantiemTax = (grossTantiem * tantiemTaxRate).toLong()
                        val netTantiem = grossTantiem - tantiemTax
                        
                        familyOfficePrivateBalanceVal += netTantiem
                        personalTaxPaidVal += tantiemTax
                        totalTaxPaidVal += tantiemTax
                        
                        monthlyLedgerRecords.add(
                            com.example.data.PrivateLedgerRecord(
                                monthTick = newMonth,
                                title = "Tantiem Bonus Tahunan kotor",
                                amount = grossTantiem,
                                isIncome = true
                            )
                        )
                        if (tantiemTax > 0) {
                            monthlyLedgerRecords.add(
                                com.example.data.PrivateLedgerRecord(
                                    monthTick = newMonth,
                                    title = "PPh Final Tantiem Tahunan",
                                    amount = tantiemTax,
                                    isIncome = false
                                )
                            )
                        }
                        
                        retainedEarningsVal = (retainedEarningsVal - actualTantiemPaid).coerceAtLeast(0L)
                        
                        val finalTantiemRateText = if (currentState.privateTaxServiceLevel == 2) "5% (Tax Lawyer Disc)" else "10%"
                        foNews.add(MarketNews(
                            id = "fo_tantiem_${System.currentTimeMillis()}",
                            text = "TANTIEM TAHUNAN: RUPS mencairkan Bonus Kinerja Akhir Tahun sebesar ${currentTantiemPercentVal}% dari Laba Tahunan ($${com.example.ui.formatCurrencyRingkas(annualNetIncome.toDouble(), false)}). Bagian kotor Anda $${com.example.ui.formatCurrencyRingkas(grossTantiem.toDouble(), false)}, dipotong PPh Final ${finalTantiemRateText} ($${com.example.ui.formatCurrencyRingkas(tantiemTax.toDouble(), false)}). Bersih $${com.example.ui.formatCurrencyRingkas(netTantiem.toDouble(), false)} ditambahkan ke Kas Pribadi.",
                            type = "BULL"
                        ))
                    }
                }
            }
        }

        // C. Kewajiban Lapor SPT & Audit Tax Haven (Setiap Bulan ke-12 / Tutup Buku)
        if (currentState.inGameMonth % 12 == 0) {
            // 0. Wealth Tax check on private stock portfolio (1% of net portfolio gains)
            var totalPrivatePortfolioValue = 0.0
            var totalPrivateCostBasis = 0.0
            currentState.privateStockPortfolio.forEach { owned ->
                val liveStock = _stockList.value.find { it.ticker == owned.ticker }
                val livePrice = liveStock?.currentPrice ?: owned.averagePrice
                totalPrivatePortfolioValue += owned.shares * livePrice
                
                val safeAveragePrice = if (owned.averagePrice <= 0.0) livePrice else owned.averagePrice
                totalPrivateCostBasis += owned.shares * safeAveragePrice
            }

            if (totalPrivatePortfolioValue > 0.0) {
                val privateGains = maxOf(0.0, totalPrivatePortfolioValue - totalPrivateCostBasis)
                val wealthTax = (privateGains * 0.01).toLong()
                if (wealthTax > 0L) {
                    familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - wealthTax).coerceAtLeast(0L)
                    personalTaxPaidVal += wealthTax
                    totalTaxPaidVal += wealthTax
                    
                    monthlyLedgerRecords.add(
                        com.example.data.PrivateLedgerRecord(
                            monthTick = newMonth,
                            title = "Pajak Kekayaan Saham (Wealth Tax)",
                            amount = wealthTax,
                            isIncome = false
                        )
                    )
                    
                    foNews.add(MarketNews(
                        id = "wealth_tax_${System.currentTimeMillis()}",
                        text = "⚖️ WEALTH TAX: Sistem mendeteksi portofolio saham pribadi Anda bernilai $${com.example.ui.formatCurrencyRingkas(totalPrivatePortfolioValue, false)}. Pajak Kekayaan sebesar 1% dari total keuntungan saham ($${com.example.ui.formatCurrencyRingkas(wealthTax.toDouble(), false)}) berhasil dipotong dari Kas Pribadi.",
                        type = "BEAR"
                    ))
                }
            }

            val sLvl = currentState.privateTaxServiceLevel
            if (sLvl == 1) {
                val feeVal = 50000L
                familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - feeVal).coerceAtLeast(0L)
                isSptReportedThisYearVal = true
                monthlyLedgerRecords.add(
                    com.example.data.PrivateLedgerRecord(
                        monthTick = newMonth,
                        title = "Biaya Jasa SPT Big Four",
                        amount = feeVal,
                        isIncome = false
                    )
                )
                foNews.add(MarketNews(
                    id = "fo_auto_spt_bigfour_${System.currentTimeMillis()}",
                    text = "💼 WEALTH MANAGEMENT: Firma Akuntansi Big Four otomatis melapor SPT Tahunan Anda. Biaya jasa tahunan sebesar $50,000 dipotong dari Kas Pribadi.",
                    type = "NEUTRAL"
                ))
            } else if (sLvl == 2) {
                val feeVal = 500000L
                familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - feeVal).coerceAtLeast(0L)
                isSptReportedThisYearVal = true
                monthlyLedgerRecords.add(
                    com.example.data.PrivateLedgerRecord(
                        monthTick = newMonth,
                        title = "Biaya Jasa Pajak Lawyer & SFO",
                        amount = feeVal,
                        isIncome = false
                    )
                )
                foNews.add(MarketNews(
                    id = "fo_auto_spt_lawyer_${System.currentTimeMillis()}",
                    text = "🕴️ SFO & TAX LAWYER: Struktur hukum dan kantor keluarga elit Anda secara otomatis mengurus dan melapor SPT Tahunan Anda tepat waktu. Biaya jasa tahunan sebesar $500,000 dipotong dari Kas Pribadi.",
                    type = "NEUTRAL"
                ))
            }

            // 1. Personal SPT Report Check
            if (!isSptReportedThisYearVal) {
                consecutiveUnreportedSptVal += 1
                val penalty = 50000L * consecutiveUnreportedSptVal
                familyOfficePrivateBalanceVal = (familyOfficePrivateBalanceVal - penalty).coerceAtLeast(0L)
                monthlyLedgerRecords.add(
                    com.example.data.PrivateLedgerRecord(
                        monthTick = newMonth,
                        title = "Denda Keterlambatan Lapor SPT Tahunan",
                        amount = penalty,
                        isIncome = false
                    )
                )
                foNews.add(MarketNews(
                    id = "fo_spt_penalty_${System.currentTimeMillis()}",
                    text = "DENDA SPT TAHUNAN: Anda terlambat melapor SPT Tahunan (${consecutiveUnreportedSptVal} tahun berturut-turut). Denda administrasi sebesar $${com.example.ui.formatCurrencyRingkas(penalty.toDouble(), false)} otomatis memotong Kas Pribadi Anda.",
                    type = "BEAR"
                ))
            }
            // Reset isSptReportedThisYear = false untuk tahun yang baru.
            isSptReportedThisYearVal = false

            // 2. Offshore Tax Haven Audit (5% annual chance on Closing)
            if (finalTaxReport.isTaxHavenActive && Math.random() < 0.05) {
                val annualNetIncome = financialHistoryVal.takeLast(12).sumOf { it.netIncome }
                val normalTaxRate = 0.20
                val normalTax = if (annualNetIncome > 0) (annualNetIncome * normalTaxRate).toLong() else 500000L
                val penaltyHolding = normalTax * 5
                
                finalTaxReport = finalTaxReport.copy(
                    unpaidTaxes = finalTaxReport.unpaidTaxes + penaltyHolding
                )
                
                foNews.add(MarketNews(
                    id = "corporate_tax_haven_audit_${System.currentTimeMillis()}",
                    text = "🚨 AUDIT HUKUM: Mega Holding Anda diaudit akibat menggunakan skema 'Offshore Tax Haven'. Denda administratif 5x lipat pajak normal senilai $${com.example.ui.formatCurrencyRingkas(penaltyHolding.toDouble(), false)} diakumulasikan ke Surat Ketetapan Pajak Kurang Bayar (SKPKB) perusahaan.",
                    type = "BEAR"
                ))
            }
        }

        // Automatic payment of Corporate Unpaid Taxes if hasNotary is active
        if (finalTaxReport.hasNotary && finalTaxReport.unpaidTaxes > 0) {
            val amountToPay = Math.min(familyOfficeCash.toLong(), finalTaxReport.unpaidTaxes)
            if (amountToPay > 0) {
                familyOfficeCash -= amountToPay
                val newUnpaid = finalTaxReport.unpaidTaxes - amountToPay
                val newFrozenId = if (newUnpaid <= 0) null else finalTaxReport.frozenBusinessId
                finalTaxReport = finalTaxReport.copy(
                    unpaidTaxes = newUnpaid,
                    frozenBusinessId = newFrozenId
                )
                foNews.add(MarketNews(
                    id = "corporate_notary_auto_pay_${System.currentTimeMillis()}",
                    text = "💼 NOTARIS OTOMATIS: Jasa Notaris & Kepatuhan Hukum otomatis membayar sebagian/seluruh tunggakan SKPKB perusahaan sebesar $${com.example.ui.formatCurrencyRingkas(amountToPay.toDouble(), false)} memotong murni Kas Holding Anda.",
                    type = "NEUTRAL"
                ))
            }
        }

        if (foNews.isNotEmpty()) {
            _newsFeed.value = (foNews + _newsFeed.value).take(20)
        }

        // ----------------------------------------------------
        // PRIVATE FOUNDATION & LEGACY TICK
        // ----------------------------------------------------
        var totalLegacyReward = 0L
        val updatedFoundations = currentState.foundations.map { f ->
            var nextConstructionMonthsLeft = f.constructionMonthsLeft
            var nextIsLegalized = f.isLegalized
            if (!f.isLegalized && f.constructionMonthsLeft > 0) {
                nextConstructionMonthsLeft -= 1
                if (nextConstructionMonthsLeft <= 0) {
                    nextIsLegalized = true
                    foNews.add(MarketNews(
                        id = "foundation_legalized_${System.currentTimeMillis()}_${f.id}",
                        text = "🏛️ YAYASAN LEGAL: Proses legalitas '${f.name}' (${f.type.label}) selesai. Anda sekarang bisa menyuntikkan dana abadi dan membangun fasilitas!",
                        type = "BULL"
                    ))
                }
            }

            var nextEndowmentFund = f.endowmentFund
            val nextFacilities = f.facilities.map { fac ->
                var nextBuildMonthsLeft = fac.buildMonthsLeft
                var nextIsOperational = fac.isOperational
                if (fac.buildMonthsLeft > 0) {
                    nextBuildMonthsLeft -= 1
                    if (nextBuildMonthsLeft <= 0) {
                        nextIsOperational = true
                        foNews.add(MarketNews(
                            id = "facility_built_${System.currentTimeMillis()}_${fac.id}",
                            text = "🏗️ FASILITAS SELESAI: Fasilitas '${fac.name}' di Yayasan ${f.name} telah selesai dibangun dan siap beroperasi!",
                            type = "BULL"
                        ))
                    }
                }

                if (nextIsOperational) {
                    if (nextEndowmentFund >= fac.monthlyOperationalCost) {
                        nextEndowmentFund -= fac.monthlyOperationalCost
                        totalLegacyReward += fac.prestigeReward
                        fac.copy(
                            buildMonthsLeft = 0,
                            isOperational = true
                        )
                    } else {
                        // Mangkrak / Tutup Sementara
                        fac.copy(
                            buildMonthsLeft = 0,
                            isOperational = false
                        )
                    }
                } else {
                    fac.copy(
                        buildMonthsLeft = nextBuildMonthsLeft,
                        isOperational = false
                    )
                }
            }

            val nextEduInstitutions = if (f.type == com.example.data.FoundationType.EDUCATION && nextIsLegalized) {
                (f.educationInstitutions ?: emptyList()).map { inst ->
                    if (inst.constructionMonthsLeft > 0) {
                        inst.copy(
                            constructionMonthsLeft = inst.constructionMonthsLeft - 1
                        )
                    } else {
                        val addedPoints = (inst.facilityLevel * 0.5) + (inst.prestigeScore * 0.1)
                        val beforePoints = inst.accreditationPoints
                        val nextPoints = Math.min(100, (beforePoints + addedPoints).toInt())

                        // 1. Pindahkan status rekrutmen ke aktif
                        val currentTeachers = inst.teachers
                        val tickedTeachers = currentTeachers.copy(
                            umum = currentTeachers.umum.copy(
                                active = currentTeachers.umum.active + currentTeachers.umum.recruiting,
                                recruiting = 0
                            ),
                            spesialis = currentTeachers.spesialis.copy(
                                active = currentTeachers.spesialis.active + currentTeachers.spesialis.recruiting,
                                recruiting = 0
                            ),
                            senior = currentTeachers.senior.copy(
                                active = currentTeachers.senior.active + currentTeachers.senior.recruiting,
                                recruiting = 0
                            )
                        )

                        val currentSupport = inst.supportStaff
                        val tickedSupport = currentSupport.copy(
                            ob = currentSupport.ob.copy(
                                active = currentSupport.ob.active + currentSupport.ob.recruiting,
                                recruiting = 0
                            ),
                            satpam = currentSupport.satpam.copy(
                                active = currentSupport.satpam.active + currentSupport.satpam.recruiting,
                                recruiting = 0
                            ),
                            admin = currentSupport.admin.copy(
                                active = currentSupport.admin.active + currentSupport.admin.recruiting,
                                recruiting = 0
                            ),
                            chef = currentSupport.chef.copy(
                                active = currentSupport.chef.active + currentSupport.chef.recruiting,
                                recruiting = 0
                            )
                        )

                        // 2. Kurangi waktu konstruksi fasilitas tambahan
                        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
                            if (fac.constructionLeftMonths > 0) {
                                fac.copy(constructionLeftMonths = fac.constructionLeftMonths - 1)
                            } else {
                                fac
                            }
                        }

                        val updatedInst = inst.copy(
                            teachers = tickedTeachers,
                            supportStaff = tickedSupport,
                            additionalFacilities = updatedFacilities
                        )

                        // 3. Gunakan extension function universal yang baru
                        val opsCost = updatedInst.calculateTotalMonthlyOpsCost()

                        val isOp = inst.isOperational
                        val monthlyRevenue = if (isOp) {
                            inst.currentStudents * inst.monthlySpp
                        } else {
                            0L
                        }
                        val netIncome = monthlyRevenue - opsCost

                        var isRunningFine = false
                        if (netIncome < 0) {
                            val deficit = kotlin.math.abs(netIncome)
                            if (nextEndowmentFund >= deficit) {
                                nextEndowmentFund -= deficit
                                isRunningFine = true
                                if (isOp) {
                                    totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                }
                            } else {
                                // Dana abadi habis, potong dari KAS PRIBADI CEO sebagai bailout darurat!
                                val remainingDeficit = deficit - nextEndowmentFund
                                nextEndowmentFund = 0L
                                if (familyOfficePrivateBalanceVal >= remainingDeficit) {
                                    familyOfficePrivateBalanceVal -= remainingDeficit
                                    isRunningFine = true
                                    if (isOp) {
                                        totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                    }
                                } else {
                                    familyOfficePrivateBalanceVal = 0L
                                }
                            }
                        } else {
                            // Yayasan Surplus (Mandiri)
                            nextEndowmentFund += netIncome
                            isRunningFine = true
                            if (isOp) {
                                totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                            }
                        }

                        if (beforePoints < 90 && nextPoints >= 90) {
                            foNews.add(MarketNews(
                                id = "edu_prestasi_nasional_${System.currentTimeMillis()}_${inst.id}",
                                text = "🎓 PRESTASI NASIONAL: Fasilitas ${inst.level} di Yayasan ${f.name} memperoleh Akreditasi Unggul! Hibah Riset & Subsidi Negara senilai +$${com.example.ui.formatCurrencyRingkas(250000.0, false)}/bln ditambahkan ke Dana Abadi.",
                                type = "BULL"
                            ))
                        }

                        if (nextPoints >= 90 && isOp && isRunningFine) {
                            nextEndowmentFund += 250000L
                        }

                        updatedInst.copy(
                            accreditationPoints = nextPoints,
                            monthlyOperationalCost = opsCost
                        )
                    }
                }
            } else {
                f.educationInstitutions ?: emptyList()
            }

            val nextHealthInstitutions = if (f.type == com.example.data.FoundationType.HEALTHCARE && nextIsLegalized) {
                (f.healthInstitutions ?: emptyList()).map { inst ->
                    if (inst.constructionMonthsLeft > 0) {
                        inst.copy(
                            constructionMonthsLeft = inst.constructionMonthsLeft - 1
                        )
                    } else {
                        val addedPoints = (inst.facilityLevel * 0.5) + (inst.prestigeScore * 0.1)
                        val beforePoints = inst.accreditationPoints
                        val nextPoints = Math.min(100, (beforePoints + addedPoints).toInt())

                        val currentMedical = inst.medicalStaff
                        val tickedMedical = currentMedical.copy(
                            perawat = currentMedical.perawat.copy(
                                active = currentMedical.perawat.active + currentMedical.perawat.recruiting,
                                recruiting = 0
                            ),
                            dokterUmum = currentMedical.dokterUmum.copy(
                                active = currentMedical.dokterUmum.active + currentMedical.dokterUmum.recruiting,
                                recruiting = 0
                            ),
                            dokterSpesialis = currentMedical.dokterSpesialis.copy(
                                active = currentMedical.dokterSpesialis.active + currentMedical.dokterSpesialis.recruiting,
                                recruiting = 0
                            )
                        )

                        val currentSupport = inst.supportStaff
                        val tickedSupport = currentSupport.copy(
                            ob = currentSupport.ob.copy(
                                active = currentSupport.ob.active + currentSupport.ob.recruiting,
                                recruiting = 0
                            ),
                            satpam = currentSupport.satpam.copy(
                                active = currentSupport.satpam.active + currentSupport.satpam.recruiting,
                                recruiting = 0
                            ),
                            admin = currentSupport.admin.copy(
                                active = currentSupport.admin.active + currentSupport.admin.recruiting,
                                recruiting = 0
                            ),
                            chef = currentSupport.chef.copy(
                                active = currentSupport.chef.active + currentSupport.chef.recruiting,
                                recruiting = 0
                            )
                        )

                        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
                            if (fac.constructionLeftMonths > 0) {
                                fac.copy(constructionLeftMonths = fac.constructionLeftMonths - 1)
                            } else {
                                fac
                            }
                        }

                        val updatedInst = inst.copy(
                            medicalStaff = tickedMedical,
                            supportStaff = tickedSupport,
                            additionalFacilities = updatedFacilities
                        )

                        val opsCost = updatedInst.calculateTotalMonthlyOpsCost()

                        val isOp = inst.isOperational
                        val monthlyRevenue = if (isOp) {
                            inst.currentPatients * inst.monthlyBillPerPatient
                        } else {
                            0L
                        }
                        val netIncome = monthlyRevenue - opsCost

                        var isRunningFine = false
                        if (netIncome < 0) {
                            val deficit = kotlin.math.abs(netIncome)
                            if (nextEndowmentFund >= deficit) {
                                nextEndowmentFund -= deficit
                                isRunningFine = true
                                if (isOp) {
                                    totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                }
                            } else {
                                val remainingDeficit = deficit - nextEndowmentFund
                                nextEndowmentFund = 0L
                                if (familyOfficePrivateBalanceVal >= remainingDeficit) {
                                    familyOfficePrivateBalanceVal -= remainingDeficit
                                    isRunningFine = true
                                    if (isOp) {
                                        totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                    }
                                } else {
                                    familyOfficePrivateBalanceVal = 0L
                                }
                            }
                        } else {
                            nextEndowmentFund += netIncome
                            isRunningFine = true
                            if (isOp) {
                                    totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                            }
                        }

                        if (beforePoints < 90 && nextPoints >= 90) {
                            foNews.add(MarketNews(
                                id = "med_prestasi_nasional_${System.currentTimeMillis()}_${inst.id}",
                                text = "🏥 PRESTASI MEDIS: Fasilitas ${inst.level} di Yayasan ${f.name} memperoleh Akreditasi Unggul! Hibah Riset & Subsidi Layanan Kesehatan senilai +$${com.example.ui.formatCurrencyRingkas(250000.0, false)}/bln ditambahkan ke Dana Abadi.",
                                type = "BULL"
                            ))
                        }

                        if (nextPoints >= 90 && isOp && isRunningFine) {
                            nextEndowmentFund += 250000L
                        }

                        updatedInst.copy(
                            accreditationPoints = nextPoints,
                            monthlyOperationalCost = opsCost
                        )
                    }
                }
            } else {
                f.healthInstitutions ?: emptyList()
            }

            val nextCharityInstitutions = if (f.type == com.example.data.FoundationType.HUMANITARIAN && nextIsLegalized) {
                (f.charityInstitutions ?: emptyList()).map { inst ->
                    if (inst.constructionLeftMonths > 0) {
                        inst.copy(
                            constructionLeftMonths = inst.constructionLeftMonths - 1
                        )
                    } else {
                        val addedPoints = (inst.facilityLevel * 0.5) + (inst.prestigeScore * 0.1)
                        val beforePoints = inst.accreditationPoints
                        val nextPoints = Math.min(100, (beforePoints + addedPoints).toInt())

                        val currentStaff = inst.charityStaff
                        val tickedStaff = currentStaff.copy(
                            relawan = currentStaff.relawan.copy(
                                active = currentStaff.relawan.active + currentStaff.relawan.recruiting,
                                recruiting = 0
                            ),
                            staffSosial = currentStaff.staffSosial.copy(
                                active = currentStaff.staffSosial.active + currentStaff.staffSosial.recruiting,
                                recruiting = 0
                            ),
                            ahliProgram = currentStaff.ahliProgram.copy(
                                active = currentStaff.ahliProgram.active + currentStaff.ahliProgram.recruiting,
                                recruiting = 0
                            )
                        )

                        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
                            if (fac.constructionLeftMonths > 0) {
                                fac.copy(constructionLeftMonths = fac.constructionLeftMonths - 1)
                            } else {
                                fac
                            }
                        }

                        val isOp = inst.isOperational
                        // Count beneficiaries helped
                        val potentialBeneficiaries = if (isOp) {
                            ((tickedStaff.relawan.active * 5 + tickedStaff.staffSosial.active * 15 + tickedStaff.ahliProgram.active * 50) * inst.getScopeMultiplier()).toInt()
                        } else {
                            0
                        }
                        val finalBeneficiaries = potentialBeneficiaries.coerceAtMost(inst.maxCapacity)

                        val updatedInst = inst.copy(
                            charityStaff = tickedStaff,
                            additionalFacilities = updatedFacilities,
                            monthlyBeneficiaries = finalBeneficiaries
                        )

                        val opsCost = updatedInst.calculateTotalMonthlyOpsCost()
                        val netIncome = -opsCost

                        var isRunningFine = false
                        if (netIncome < 0) {
                            val deficit = kotlin.math.abs(netIncome)
                            if (nextEndowmentFund >= deficit) {
                                nextEndowmentFund -= deficit
                                isRunningFine = true
                                if (isOp) {
                                    totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                }
                            } else {
                                val remainingDeficit = deficit - nextEndowmentFund
                                nextEndowmentFund = 0L
                                if (familyOfficePrivateBalanceVal >= remainingDeficit) {
                                    familyOfficePrivateBalanceVal -= remainingDeficit
                                    isRunningFine = true
                                    if (isOp) {
                                        totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                                    }
                                } else {
                                    familyOfficePrivateBalanceVal = 0L
                                }
                            }
                        } else {
                            isRunningFine = true
                            if (isOp) {
                                totalLegacyReward += (inst.prestigeScore * 0.1).toLong()
                            }
                        }

                        if (beforePoints < 90 && nextPoints >= 90) {
                            foNews.add(MarketNews(
                                id = "charity_prestasi_${System.currentTimeMillis()}_${inst.id}",
                                text = "🧡 PRESTASI SOSIAL: Badan Amal ${inst.name} (${inst.level}) mencapai Akreditasi Unggul! Hibah kemanusiaan global senilai +$150.000/bln disalurkan ke Dana Abadi.",
                                type = "BULL"
                            ))
                        }

                        if (nextPoints >= 90 && isOp && isRunningFine) {
                            nextEndowmentFund += 150000L
                        }

                        updatedInst.copy(
                            accreditationPoints = nextPoints
                        )
                    }
                }
            } else {
                f.charityInstitutions ?: emptyList()
            }

            f.copy(
                constructionMonthsLeft = nextConstructionMonthsLeft,
                isLegalized = nextIsLegalized,
                endowmentFund = nextEndowmentFund,
                facilities = nextFacilities,
                educationInstitutions = nextEduInstitutions,
                healthInstitutions = nextHealthInstitutions,
                charityInstitutions = nextCharityInstitutions
            )
        }
        val nextLegacyPoints = currentState.foundationLegacyPoints + totalLegacyReward

        if (foNews.isNotEmpty()) {
            _newsFeed.value = (foNews + _newsFeed.value).take(20)
        }

        // Append monthly ledger records to privateLedgerHistory with a max of 200 items (FIFO)
        var updatedLedgerHistory = currentState.privateLedgerHistory + monthlyLedgerRecords
        if (updatedLedgerHistory.size > 200) {
            updatedLedgerHistory = updatedLedgerHistory.drop(updatedLedgerHistory.size - 200)
        }

        _playerState.value = currentState.copy(
            cash = familyOfficeCash,
            netWorth = newNetWorth,
            inGameMonth = newMonth,
            inGameYear = newYear,
            lastMonthIncome = monthlyIncome,
            lastMonthExpenses = monthlyExpenses,
            lastMonthNetProfit = netProfit,
            activeStartupInvestments = remainingStartups,
            taxLegalReport = finalTaxReport,
            timeDeposits = remainingDeposits,
            ownedBusinesses = familyOfficeBusinesses,
            holdingCompanies = familyOfficeHoldings,
            ownedProperties = familyOfficeProperties,
            ownedHouses = familyOfficeHouses,
            ownedCollections = familyOfficeCollections,
            personalDebt = familyOfficeDebt,
            activeTvPrograms = if (tvProgHasChanges) updatedTvProgs else currentState.activeTvPrograms,
            ipLibraryHistory = if (newIpLibraryItems.isNotEmpty()) finalIpLibrary else currentState.ipLibraryHistory,
            appProjects = if (appProjHasChanges) updatedAppProjects else currentState.appProjects,
            currentCeoSalaryPercent = currentCeoSalaryPercentVal,
            pendingCeoSalaryPercent = pendingCeoSalaryPercentVal,
            boardApprovalMonthsLeft = boardApprovalMonthsLeftVal,
            lastSalaryRequestMonth = lastSalaryRequestMonthVal,
            boardReplyMessage = boardReplyMessageVal,
            privateBalance = familyOfficePrivateBalanceVal,
            currentDividendPercent = currentDividendPercentVal,
            pendingDividendPercent = pendingDividendPercentVal,
            dividendApprovalMonthsLeft = dividendApprovalMonthsLeftVal,
            lastDividendRequestMonth = lastDividendRequestMonthVal,
            currentTantiemPercent = currentTantiemPercentVal,
            pendingTantiemPercent = pendingTantiemPercentVal,
            tantiemApprovalMonthsLeft = tantiemApprovalMonthsLeftVal,
            retainedEarnings = retainedEarningsVal,
            totalTaxPaid = totalTaxPaidVal,
            corporateTaxPaid = corporateTaxPaidVal,
            personalTaxPaid = personalTaxPaidVal,
            isSptReportedThisYear = isSptReportedThisYearVal,
            consecutiveUnreportedSpt = consecutiveUnreportedSptVal,
            financialHistory = financialHistoryVal,
            privateLedgerHistory = updatedLedgerHistory,
            foundations = updatedFoundations,
            foundationLegacyPoints = nextLegacyPoints
        )
        _playerState.value = syncTvValuation(_playerState.value)
        if(!isOffline) saveState(_playerState.value)
    }

    
    fun addProperty(name: String, location: String, price: Long, rental: Long, imageUrl: String = "") {
        val finalUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80" else imageUrl
        
        val newPropId = "prop_custom_${System.currentTimeMillis()}"
        val newProp = com.example.data.PropertyItem(newPropId, name, location, "Custom", price, rental)
        val currentMarket = _realEstateMarket.value.toMutableList()
        currentMarket.add(newProp)
        _realEstateMarket.value = currentMarket
        saveProperties(currentMarket)
        
        val newHs = com.example.data.HousingItem(newPropId, name, location, "Custom Property", price, rental, imageUrl = finalUrl)
        val currentHs = _housingList.value.toMutableList()
        currentHs.add(newHs)
        _housingList.value = currentHs
        saveHousing(currentHs)
    }

    fun addCollectionItem(categoryId: String, name: String, desc: String, price: Long, imageUrl: String = "", releaseYear: Int? = null) {
        val newId = "col_custom_${System.currentTimeMillis()}"
        val newItem = com.example.data.CollectionItem(newId, categoryId, name, desc, price, imageUrl, releaseYear)
        val currentList = _collectionList.value.toMutableList()
        currentList.add(newItem)
        _collectionList.value = currentList
        saveCollections(currentList)
    }

    fun removeCollectionItem(itemId: String) {
        val currentList = _collectionList.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _collectionList.value = currentList
        saveCollections(currentList)
    }

    fun removeProperty(propertyId: String) {
        val currentMarket = _realEstateMarket.value.toMutableList()
        currentMarket.removeAll { it.id == propertyId }
        _realEstateMarket.value = currentMarket
        saveProperties(currentMarket)
    }

    fun updateCollectionImageUrl(itemId: String, newUrl: String) {
        val currentList = _collectionList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(imageUrl = newUrl)
            _collectionList.value = currentList
            saveCollections(currentList)
        }
    }

    fun buyMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val totalCost = (metal.currentPrice * amount).toLong()
        val currentState = _playerState.value
        
        if (currentState.privateBalance >= totalCost) {
            val currentOwned = currentState.ownedMetals[metalId] ?: 0.0
            val currentAvg = currentState.ownedMetalsAveragePrices[metalId] ?: 0.0
            val newTotalAmount = currentOwned + amount
            val newAvg = if (newTotalAmount > 0) ((currentAvg * currentOwned) + (metal.currentPrice * amount)) / newTotalAmount else metal.currentPrice

            val updatedMetals = currentState.ownedMetals.toMutableMap()
            updatedMetals[metalId] = newTotalAmount
            val updatedAvgs = currentState.ownedMetalsAveragePrices.toMutableMap()
            updatedAvgs[metalId] = newAvg

            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance - totalCost,
                ownedMetals = updatedMetals,
                ownedMetalsAveragePrices = updatedAvgs
            )
            val loggedState = logToPrivateLedger(nextState, "Beli Logam Mulia ($metalId)", totalCost, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun sellMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val currentState = _playerState.value
        val currentOwned = currentState.ownedMetals[metalId] ?: 0.0
        
        if (currentOwned >= amount) {
            val totalRevenue = (metal.currentPrice * amount).toLong()
            val updatedMetals = currentState.ownedMetals.toMutableMap()
            updatedMetals[metalId] = currentOwned - amount
            val updatedAvgs = currentState.ownedMetalsAveragePrices.toMutableMap()
            if (updatedMetals[metalId]!! <= 0.0001) {
                updatedMetals.remove(metalId) // Cleanup floating point issues
                updatedAvgs.remove(metalId)
            }

            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance + totalRevenue,
                ownedMetals = updatedMetals,
                ownedMetalsAveragePrices = updatedAvgs
            )
            val loggedState = logToPrivateLedger(nextState, "Jual Logam Mulia ($metalId)", totalRevenue, true)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun openTimeDeposit(principal: Long, durationMonths: Int) {
        val currentState = _playerState.value
        if (currentState.privateBalance >= principal && principal > 0) {
            val interestRate = when (durationMonths) {
                3 -> 0.05 // 5% total
                6 -> 0.12 // 12% total
                12 -> 0.30 // 30% total
                else -> 0.0
            }
            
            val newDeposit = com.example.data.TimeDeposit(
                id = java.util.UUID.randomUUID().toString(),
                principal = principal,
                durationMonths = durationMonths,
                monthsRemaining = durationMonths,
                interestRate = interestRate
            )
            
            val updatedDeposits = currentState.timeDeposits.toMutableList()
            updatedDeposits.add(newDeposit)
            
            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance - principal,
                timeDeposits = updatedDeposits
            )
            val loggedState = logToPrivateLedger(nextState, "Buka Deposito Berjangka ($durationMonths Bln)", principal, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun withdrawTimeDeposit(depositId: String, isEarly: Boolean) {
        val currentState = _playerState.value
        val deposit = currentState.timeDeposits.find { it.id == depositId } ?: return
        
        val returnAmount = if (isEarly) {
            // Apply 5% penalty on principal
            (deposit.principal * 0.95).toLong()
        } else {
            // Full principal + interest
            (deposit.principal + (deposit.principal * deposit.interestRate)).toLong()
        }
        
        val updatedDeposits = currentState.timeDeposits.filter { it.id != depositId }
        val label = if (isEarly) "Pencairan Deposito Lebih Awal" else "Pencairan Deposito Jatuh Tempo"
        
        val nextState = currentState.copy(
            privateBalance = currentState.privateBalance + returnAmount,
            timeDeposits = updatedDeposits
        )
        val loggedState = logToPrivateLedger(nextState, label, returnAmount, true)
        _playerState.value = loggedState
        saveState(loggedState)
    }

    fun getBusinessSlotUpgradePrice(): Long {
        val extraSlots = _playerState.value.maxBusinessSlots - 11
        var price = 1_000_000.0 // 1 Million
        for (i in 0 until extraSlots) {
            price *= 1.5
        }
        return price.toLong()
    }

    fun addCash(amount: Long) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(cash = currentState.cash + amount)
    }

    fun syncContentCreator(level: Int, income: Long) {
        val currentState = _playerState.value
        val newBusinesses = currentState.ownedBusinesses.map { owned ->
            if (owned.catalogId == "content_creator") {
                owned.copy(level = level, customRevenue = income)
            } else {
                owned
            }
        }
        if (newBusinesses != currentState.ownedBusinesses) {
            _playerState.value = currentState.copy(ownedBusinesses = newBusinesses)
        }
    }

    fun injectCashToContentCreator(amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash >= amount && amount > 0) {
            val newBusinesses = currentState.ownedBusinesses.map { owned ->
                if (owned.catalogId == "content_creator") {
                    owned.copy(contentCreatorCash = owned.contentCreatorCash + amount)
                } else {
                    owned
                }
            }
            val nextState = currentState.copy(
                cash = currentState.cash - amount,
                ownedBusinesses = newBusinesses
            )
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun withdrawCashFromContentCreator(amount: Long): Boolean {
        val currentState = _playerState.value
        val cc = currentState.ownedBusinesses.find { it.catalogId == "content_creator" } ?: return false
        if (cc.contentCreatorCash >= amount && amount > 0) {
            val newBusinesses = currentState.ownedBusinesses.map { owned ->
                if (owned.catalogId == "content_creator") {
                    owned.copy(contentCreatorCash = owned.contentCreatorCash - amount)
                } else {
                    owned
                }
            }
            val nextState = currentState.copy(
                cash = currentState.cash + amount,
                ownedBusinesses = newBusinesses
            )
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun deleteContentCreatorBusiness() {
        val currentState = _playerState.value
        val newBusinesses = currentState.ownedBusinesses.filter { it.catalogId != "content_creator" }
        val nextState = currentState.copy(ownedBusinesses = newBusinesses)
        _playerState.value = nextState
        saveState(nextState)
    }

    fun levelUpContentCreator(): Boolean {
        val currentState = _playerState.value
        val cc = currentState.ownedBusinesses.find { it.catalogId == "content_creator" } ?: return false
        if (cc.level >= 100) return false
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked) return false

        val cost = (500.0 * Math.pow(1.18, (cc.level - 1).toDouble())).toLong()
        if (cc.contentCreatorCash >= cost) {
            val newLevel = cc.level + 1
            val newSubs = cc.contentCreatorSubscribers + (100.0 * Math.pow(1.16, newLevel.toDouble())).toLong()
            val newBusinesses = currentState.ownedBusinesses.map { owned ->
                if (owned.catalogId == "content_creator") {
                    owned.copy(
                        level = newLevel,
                        contentCreatorSubscribers = newSubs,
                        contentCreatorCash = owned.contentCreatorCash - cost
                    )
                } else {
                    owned
                }
            }
            val nextState = currentState.copy(ownedBusinesses = newBusinesses)
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun hireEmployeeContentCreator(): Boolean {
        val currentState = _playerState.value
        val cc = currentState.ownedBusinesses.find { it.catalogId == "content_creator" } ?: return false
        val maxEmp = when {
            cc.level >= 81 -> 100
            cc.level >= 61 -> 50
            cc.level >= 41 -> 20
            cc.level >= 21 -> 5
            else -> 0
        }
        if (cc.contentCreatorEmployees >= maxEmp) return false

        val cost = (1500.0 * Math.pow(1.2, cc.contentCreatorEmployees.toDouble())).toLong()
        if (cc.contentCreatorCash >= cost) {
            val newBusinesses = currentState.ownedBusinesses.map { owned ->
                if (owned.catalogId == "content_creator") {
                    owned.copy(
                        contentCreatorEmployees = owned.contentCreatorEmployees + 1,
                        contentCreatorCash = owned.contentCreatorCash - cost
                    )
                } else {
                    owned
                }
            }
            val nextState = currentState.copy(ownedBusinesses = newBusinesses)
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun unlockOfficeContentCreator(): Boolean {
        val currentState = _playerState.value
        val cc = currentState.ownedBusinesses.find { it.catalogId == "content_creator" } ?: return false
        val cost = 5_000_000L
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked && cc.contentCreatorCash >= cost) {
            val newBusinesses = currentState.ownedBusinesses.map { owned ->
                if (owned.catalogId == "content_creator") {
                    owned.copy(
                        contentCreatorOfficeUnlocked = true,
                        contentCreatorCash = owned.contentCreatorCash - cost
                    )
                } else {
                    owned
                }
            }
            val nextState = currentState.copy(ownedBusinesses = newBusinesses)
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun deductCash(amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash >= amount) {
            _playerState.value = currentState.copy(cash = currentState.cash - amount)
            return true
        }
        return false
    }

    fun upgradeBusinessSlot() {
        val currentState = _playerState.value
        val cost = getBusinessSlotUpgradePrice()
        if (currentState.cash >= cost) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                maxBusinessSlots = currentState.maxBusinessSlots + 1
            )
        }
    }

    fun buyProperty(propertyId: String) {
        val currentState = _playerState.value
        val property = _realEstateMarket.value.find { it.id == propertyId }
        val cost = property?.currentPrice ?: 0L
        if (property != null && currentState.privateBalance >= cost) {
            val newBalance = currentState.privateBalance - cost
            val ownedList = currentState.ownedProperties.toMutableList()
            ownedList.add(com.example.data.OwnedProperty(propertyId, cost, property.basePrice, property.condition))
            
            // Tax impact handled implicitly because RealEstate value adds to netWorth and wealth tax
            val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = ownedList)
            val loggedState = logToPrivateLedger(nextState, "Beli Properti Komersial ($propertyId)", cost, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun getRenovationCost(property: com.example.data.PropertyItem, condition: Int): Long {
        val missingCondition = 100 - condition
        return ((property.basePrice * missingCondition) / 200.0).toLong()
    }

    fun renovateProperty(propertyId: String) {
        val currentState = _playerState.value
        val ownedProp = currentState.ownedProperties.find { it.propertyId == propertyId }
        val propItem = _realEstateMarket.value.find { it.id == propertyId }
        if (ownedProp != null && propItem != null && ownedProp.condition < 100) {
            val cost = getRenovationCost(propItem, ownedProp.condition)
            if (currentState.privateBalance >= cost) {
                val newBalance = currentState.privateBalance - cost
                val newEstimatedValue = (propItem.basePrice * 1.5).toLong()
                val updatedProp = ownedProp.copy(condition = 100, currentEstimatedValue = newEstimatedValue)
                val newList = currentState.ownedProperties.map { if (it.propertyId == propertyId) updatedProp else it }
                val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = newList)
                val loggedState = logToPrivateLedger(nextState, "Renovasi Properti ($propertyId)", cost, false)
                _playerState.value = loggedState
                saveState(loggedState)
            }
        }
    }

    fun sellPropertySultan(propertyId: String) {
        val currentState = _playerState.value
        val ownedProp = currentState.ownedProperties.find { it.propertyId == propertyId }
        val propItem = _realEstateMarket.value.find { it.id == propertyId }
        if (ownedProp != null && propItem != null && ownedProp.condition == 100) {
            val sellPrice = ownedProp.currentEstimatedValue
            val newBalance = currentState.privateBalance + sellPrice
            val newList = currentState.ownedProperties.filterNot { it.propertyId == propertyId }
            val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = newList)
            val loggedState = logToPrivateLedger(nextState, "Jual Properti Komersial ($propertyId)", sellPrice, true)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun buyCollection(itemId: String): String? {
        val currentState = _playerState.value
        val item = _collectionList.value.find { it.id == itemId } ?: return "Item tidak ditemukan."
        if (currentState.privateBalance < item.basePrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        val newBalance = currentState.privateBalance - item.basePrice
        val ownedList = currentState.ownedCollections.toMutableList()
        ownedList.add(com.example.data.OwnedCollection(itemId = itemId, purchasedPrice = item.basePrice))
        val nextState = currentState.copy(privateBalance = newBalance, ownedCollections = ownedList)
        val newState = logToPrivateLedger(nextState, "Beli Koleksi (${item.name})", item.basePrice, false)
        _playerState.value = newState
        saveState(newState)
        return null
    }
    
    fun updateCollectionImage(instanceId: String, imageUrl: String) {
        val currentState = _playerState.value
        val updatedCollections = currentState.ownedCollections.map { 
            if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
        }
        _playerState.value = currentState.copy(ownedCollections = updatedCollections)
    }

    fun buyResidentialProperty(housingId: String): String? {
        val currentState = _playerState.value
        val currentMarket = _housingList.value
        val item = currentMarket.find { it.id == housingId }
        
        if (item == null) {
            return "Properti tidak ditemukan di Marketplace."
        }
        if (currentState.privateBalance < item.buyPrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        
        // Add to owned
        val newBalance = currentState.privateBalance - item.buyPrice
        val ownedList = currentState.ownedHouses.toMutableList()
        val existing = ownedList.find { it.housingId == housingId }
        if (existing != null) {
            return "Anda sudah memiliki properti ini."
        }
        
        ownedList.add(com.example.data.OwnedHousing(housingId = housingId, purchasedPrice = item.buyPrice, customImageUrl = item.imageUrl))
        val nextState = currentState.copy(privateBalance = newBalance, ownedHouses = ownedList)
        val newState = logToPrivateLedger(nextState, "Beli Properti Hunian (${item.name})", item.buyPrice, false)
        _playerState.value = newState
        saveState(newState)
        
        return null
    }

    fun rentHousing(housingId: String): String? {
        val currentState = _playerState.value
        val item = _housingList.value.find { it.id == housingId }
        if (item == null) {
            return "Properti tidak ditemukan."
        }
        if (currentState.privateBalance < item.rentPrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        val newBalance = currentState.privateBalance - item.rentPrice
        val rentedList = currentState.rentedHouses.toMutableList()
        rentedList.add(com.example.data.RentedHousing(housingId = housingId, monthlyRent = item.rentPrice))
        val nextState = currentState.copy(privateBalance = newBalance, rentedHouses = rentedList)
        val newState = logToPrivateLedger(nextState, "Sewa Properti Hunian (${item.name})", item.rentPrice, false)
        _playerState.value = newState
        saveState(newState)
        return null
    }

    fun updateHousingImage(instanceId: String, imageUrl: String, isRented: Boolean) {
        val currentState = _playerState.value
        if (isRented) {
            val updated = currentState.rentedHouses.map { 
                if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
            }
            _playerState.value = currentState.copy(rentedHouses = updated)
        } else {
            val updated = currentState.ownedHouses.map { 
                if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
            }
            _playerState.value = currentState.copy(ownedHouses = updated)
        }
    }

    fun sellHousing(instanceId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val itemToSell = currentState.ownedHouses.find { it.instanceId == instanceId }
        
        if (itemToSell != null) {
            val updatedOwnedHouses = currentState.ownedHouses.filter { it.instanceId != instanceId }
            val newBalance = currentState.privateBalance + sellPrice
            val nextState = currentState.copy(
                privateBalance = newBalance,
                ownedHouses = updatedOwnedHouses,
                lastMonthIncome = currentState.lastMonthIncome + sellPrice
            )
            
            val itemInfo = _housingList.value.find { it.id == itemToSell.housingId }
            val nameLabel = itemInfo?.name ?: instanceId
            val newState = logToPrivateLedger(nextState, "Jual Properti Hunian ($nameLabel)", sellPrice, true)
            _playerState.value = newState
            saveState(newState)
            
            // Re-add to market
            if (itemInfo == null) {
                // If it was custom or removed, let's try to restore? No, if it's missing from market, just leave it sold
            } else {
                val currentMarket = _housingList.value.toMutableList()
                if (currentMarket.none { it.id == itemToSell.housingId }) {
                    currentMarket.add(itemInfo)
                    _housingList.value = currentMarket
                    saveHousing(currentMarket)
                }
            }
        }
    }

    fun sellCollection(instanceId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val itemToSell = currentState.ownedCollections.find { it.instanceId == instanceId }
        
        if (itemToSell != null) {
            val updatedCollections = currentState.ownedCollections.filter { it.instanceId != instanceId }
            val newBalance = currentState.privateBalance + sellPrice
            val nextState = currentState.copy(
                privateBalance = newBalance,
                ownedCollections = updatedCollections,
                lastMonthIncome = currentState.lastMonthIncome + sellPrice
            )
            
            val itemInfo = _collectionList.value.find { it.id == itemToSell.itemId }
            val nameLabel = itemInfo?.name ?: instanceId
            val newState = logToPrivateLedger(nextState, "Jual Koleksi ($nameLabel)", sellPrice, true)
            _playerState.value = newState
            saveState(newState)
        }
    }

    // --- TAX & LEGAL ---
    
    fun toggleNotary(enabled: Boolean) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(hasNotary = enabled)
        )
    }
    
    fun payTaxesManually(amount: Long) {
        val currentState = _playerState.value
        if (currentState.cash >= amount && currentState.taxLegalReport.unpaidTaxes >= amount) {
            val newUnpaid = currentState.taxLegalReport.unpaidTaxes - amount
            val newFrozenId = if (newUnpaid <= 0) null else currentState.taxLegalReport.frozenBusinessId
            _playerState.value = currentState.copy(
                cash = currentState.cash - amount,
                taxLegalReport = currentState.taxLegalReport.copy(
                    unpaidTaxes = newUnpaid,
                    frozenBusinessId = newFrozenId
                )
            )
        }
    }
    
    fun resolveLawsuit(lawsuitId: String, lawyerTier: Int) {
        // lawyerTier: 0 = No Lawyer, 1 = Intern (40% win), 2 = Premium (95% win)
        val currentState = _playerState.value
        val lawsuit = currentState.taxLegalReport.activeLawsuits.find { it.id == lawsuitId } ?: return
        
        val scale = lawsuit.scaleFactor
        var isWon = false
        var totalCost = 0L

        when (lawyerTier) {
            1 -> {
                val lawyerFee = (scale * 0.1).toLong()
                isWon = Math.random() < 0.40
                totalCost = lawyerFee + if (isWon) 0L else scale
            }
            2 -> {
                val lawyerFee = (scale * 0.4).toLong()
                isWon = Math.random() < 0.95
                totalCost = lawyerFee + if (isWon) 0L else scale
            }
            else -> {
                // Settle outside court (pay full + extra fine)
                totalCost = (scale * 1.5).toLong()
            }
        }
        
        if (currentState.cash >= totalCost) {
            val updatedLawsuits = currentState.taxLegalReport.activeLawsuits.filterNot { it.id == lawsuitId }
            _playerState.value = currentState.copy(
                cash = currentState.cash - totalCost,
                taxLegalReport = currentState.taxLegalReport.copy(activeLawsuits = updatedLawsuits)
            )
        }
    }

    fun toggleTaxHaven() {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(
                isTaxHavenActive = !currentState.taxLegalReport.isTaxHavenActive
            )
        )
    }

    fun payUnpaidTaxes() {
        val currentState = _playerState.value
        if (currentState.cash >= currentState.taxLegalReport.unpaidTaxes) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - currentState.taxLegalReport.unpaidTaxes,
                taxLegalReport = currentState.taxLegalReport.copy(unpaidTaxes = 0, frozenBusinessId = null)
            )
        }
    }

    fun liquidateBusiness(instanceId: String) {
        val currentState = _playerState.value
        var isNested = false
        var parentHoldingId: String? = null
        
        var business = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        
        if (business == null) {
            for (holding in currentState.holdingCompanies) {
                business = holding.subsidiaries.find { it.instanceId == instanceId }
                if (business != null) {
                    isNested = true
                    parentHoldingId = holding.instanceId
                    break
                }
            }
        }
        
        if (business == null) return
        val catalogItem = getCatalogItem(business.catalogId, currentState) ?: return
        
        val valuation = if (business.catalogId == "aviation_group") {
            val fleetVal = business.airlineFleetComplex.sumOf { pl ->
                val pDef = com.example.data.AVIATION_AIRCRAFT_CATALOG.find { it.id == pl.modelId }
                if (pl.isLeased) 0L else (pDef?.price ?: 0L)
            }
            val hubsVal = business.airlineHubsComplex.sumOf { it.baseCost }
            val businessCashVal = business.companyCash.toLong()
            val baseVal = catalogItem.costToBuy
            val totalAssets = baseVal + fleetVal + hubsVal + businessCashVal
            (totalAssets * 0.70).toLong()
        } else {
            com.example.data.getBusinessValuation(business, catalogItem)
        }
        
        if (isNested && parentHoldingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == parentHoldingId) {
                    holding.copy(
                        holdingCash = holding.holdingCash + valuation,
                        subsidiaries = holding.subsidiaries.filter { it.instanceId != instanceId }
                    )
                } else holding
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        } else {
            _playerState.value = currentState.copy(
                cash = currentState.cash + valuation,
                ownedBusinesses = currentState.ownedBusinesses.filter { it.instanceId != instanceId }
            )
        }
    }

    fun demergerHolding(holdingInstanceId: String) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingInstanceId } ?: return
        
        val children = holding.subsidiaries.map { it.copy(instanceId = java.util.UUID.randomUUID().toString()) }
        
        _playerState.value = currentState.copy(
            holdingCompanies = currentState.holdingCompanies.filter { it.instanceId != holdingInstanceId },
            ownedBusinesses = currentState.ownedBusinesses + children
        )
    }

    fun mergeBusinesses(businessIds: List<String>, mergerName: String, mergerType: String): Boolean {
        if (businessIds.size < 2) return false
        
        val currentState = _playerState.value
        val businessesToMerge = currentState.ownedBusinesses.filter { businessIds.contains(it.instanceId) || businessIds.contains(it.catalogId) }
        
        // Guard clause: Perusahaan hasil akuisisi pasar modal TIDAK BOLEH digabungkan
        if (businessesToMerge.any { it.acquiredStockTicker != null }) {
            return false
        }
        
        var totalValuation = 0L
        businessesToMerge.forEach { owned ->
            val catalogItem = getCatalogItem(owned.catalogId, currentState) ?: return@forEach
            totalValuation += getBusinessValuation(owned, catalogItem)
        }
        
        val mergeFee = (totalValuation * 0.10).toLong() // 10% legal fee for IPO/Merger
        if (currentState.cash < mergeFee) return false
        
        val finalName = if (mergerName.contains(mergerType, ignoreCase = true)) mergerName else "$mergerName $mergerType"
        val newHolding = com.example.data.HoldingCompany(
            name = finalName,
            subsidiaries = businessesToMerge
        )
        
        val newOwnedList = currentState.ownedBusinesses.filterNot { owned -> businessIds.contains(owned.instanceId) || businessIds.contains(owned.catalogId) }.toMutableList()
        val newHoldingsList = currentState.holdingCompanies.toMutableList()
        newHoldingsList.add(newHolding)
       
        _playerState.value = currentState.copy(
            cash = currentState.cash - mergeFee,
            ownedBusinesses = newOwnedList,
            holdingCompanies = newHoldingsList
        )
        saveState(_playerState.value)
        return true
    }

    fun processIPO(holdingId: String, percentToSell: Float) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return
        
        if (holding.isPublic) return // Already went public
        
        val (updatedHolding, cashGained) = com.example.data.CorporateFinanceManager.processIPO(holding, percentToSell, currentState)
        
        val updatedHoldings = currentState.holdingCompanies.map { 
            if(it.instanceId == holdingId) updatedHolding else it 
        }
        
        _playerState.value = currentState.copy(
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + cashGained
        )
        saveState(_playerState.value)
    }

    fun restructureBusiness(sourceInstanceId: String, targetId: String, isTargetHolding: Boolean) {
        val currentState = _playerState.value
        if (isTargetHolding) {
            val sourceBusiness = currentState.ownedBusinesses.find { it.instanceId == sourceInstanceId } ?: return
            val updatedBusinesses = currentState.ownedBusinesses.filter { it.instanceId != sourceInstanceId }
            val updatedHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == targetId) {
                    holding.copy(subsidiaries = holding.subsidiaries + sourceBusiness.copy(parentId = targetId))
                } else {
                    holding
                }
            }
            _playerState.value = currentState.copy(
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        } else {
            val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
                if (biz.instanceId == sourceInstanceId) {
                    biz.copy(parentId = targetId)
                } else {
                    biz
                }
            }
            _playerState.value = currentState.copy(ownedBusinesses = updatedBusinesses)
        }
        saveState(_playerState.value)
    }

    fun ejectBusinessToRoot(sourceInstanceId: String, holdingId: String) {
        val currentState = _playerState.value
        
        var foundBusiness: com.example.data.OwnedBusiness? = null
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            if (holding.instanceId == holdingId) {
                val match = holding.subsidiaries.find { it.instanceId == sourceInstanceId }
                if (match != null) {
                    foundBusiness = match.copy(parentId = null)
                }
                holding.copy(subsidiaries = holding.subsidiaries.filter { it.instanceId != sourceInstanceId })
            } else {
                holding
            }
        }
        
        val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == sourceInstanceId) {
                biz.copy(parentId = null)
            } else {
                biz
            }
        }.toMutableList()
        
        foundBusiness?.let { extracted ->
            if (!updatedBusinesses.any { it.instanceId == sourceInstanceId }) {
                updatedBusinesses.add(extracted)
            }
        }
        
        _playerState.value = currentState.copy(
            ownedBusinesses = updatedBusinesses,
            holdingCompanies = updatedHoldings
        )
        saveState(_playerState.value)
    }

    fun renameHoldingCompany(holdingId: String, newName: String) {
        val currentState = _playerState.value
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            if (holding.instanceId == holdingId) {
                holding.copy(name = newName)
            } else {
                holding
            }
        }
        _playerState.value = currentState.copy(holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
    }

    fun formMegaHolding(name: String, includeInvestments: Boolean, investmentCompanyName: String = "") {
        val currentState = _playerState.value
        val newState = currentState.copy(
            megaHolding = com.example.data.MegaHoldingState(
                isActive = true,
                companyName = name,
                includesInvestments = includeInvestments,
                investmentCompanyName = investmentCompanyName,
                ownershipPercentage = 100.0
            )
        )
        _playerState.value = newState
        saveState(newState)
    }

    fun ipoMegaHolding(percentageToSell: Double) {
        val currentState = _playerState.value
        if (!currentState.megaHolding.isActive || percentageToSell <= 0 || percentageToSell > currentState.megaHolding.ownershipPercentage) return
        
        val businessValue = currentState.ownedBusinesses.sumOf {
            val catalogItem = getCatalogItem(it.catalogId, currentState)
            if (catalogItem != null) getBusinessValuation(it, catalogItem) else 0L
        }
        val holdingValue = currentState.holdingCompanies.sumOf { holding ->
            val subVal = holding.subsidiaries.sumOf { sub ->
                val catalogItem = getCatalogItem(sub.catalogId, currentState)
                if (catalogItem != null) getBusinessValuation(sub, catalogItem) else 0L
            }
            subVal
        }
        var baseMegaValuation = businessValue + holdingValue
        if (currentState.megaHolding.includesInvestments) {
            val stocksValue = currentState.ownedStocks.sumOf { owned ->
                val liveStock = _stockList.value.find { it.ticker == owned.ticker }
                val livePrice = liveStock?.currentPrice ?: owned.averagePrice
                (owned.shares * livePrice).toLong()
            }
            baseMegaValuation += stocksValue
        }
        
        val cashGained = (baseMegaValuation * (percentageToSell / 100.0)).toLong()
        
        val newState = currentState.copy(
            cash = currentState.cash + cashGained,
            megaHolding = currentState.megaHolding.copy(
                ownershipPercentage = currentState.megaHolding.ownershipPercentage - percentageToSell
            )
        )
        _playerState.value = newState
        saveState(newState)
    }

    fun processDivestment(holdingId: String) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return
        
        val cashGained = com.example.data.CorporateFinanceManager.processDivestment(holding, currentState)
        val updatedHoldings = currentState.holdingCompanies.filterNot { it.instanceId == holdingId }
        
        _playerState.value = currentState.copy(
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + cashGained
        )
        saveState(_playerState.value)
    }

    fun buyBusinessForHolding(holdingId: String, businessId: String, customName: String? = null, studioType: String = "LIVE_ACTION", vendorId: String? = null, firstHub: String? = null) {
        val currentState = _playerState.value
        val catalogItem = getCatalogItem(businessId, currentState) ?: return
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return

        if (currentState.cash >= catalogItem.costToBuy) {
            var newOwnedBusiness = com.example.data.OwnedBusiness(
                catalogId = businessId,
                customName = customName,
                level = 1,
                purchasedUpgrades = emptySet(),
                upgradeLevels = emptyMap(),
                studioType = studioType,
                airlineHubs = if (firstHub != null) listOf(firstHub) else emptyList()
            )
            
            var updatedBusinesses = currentState.ownedBusinesses
            var updatedHoldings = currentState.holdingCompanies
            
            if (vendorId != null) {
                val profitForConstruction = (catalogItem.costToBuy * 0.40)
                newOwnedBusiness = newOwnedBusiness.copy(
                    isUpgrading = true,
                    upgradeDelayMonths = 3
                )
                
                updatedBusinesses = updatedBusinesses.map { biz ->
                    if (biz.instanceId == vendorId) {
                        val newTender = com.example.data.ConstructionProject(
                            name = "Internal: ${customName ?: catalogItem.name}",
                            totalContractValue = profitForConstruction,
                            durationMonths = 3,
                            remainingMonths = 3
                        )
                        biz.copy(
                            companyCash = biz.companyCash + profitForConstruction.toLong(),
                            activeTenders = biz.activeTenders + newTender
                        )
                    } else biz
                }
                
                updatedHoldings = updatedHoldings.map { h ->
                    val newSubs = h.subsidiaries.map { biz ->
                        if (biz.instanceId == vendorId) {
                            val newTender = com.example.data.ConstructionProject(
                                name = "Internal: ${customName ?: catalogItem.name}",
                                totalContractValue = profitForConstruction,
                                durationMonths = 3,
                                remainingMonths = 3
                            )
                            biz.copy(
                                companyCash = biz.companyCash + profitForConstruction.toLong(),
                                activeTenders = biz.activeTenders + newTender
                            )
                        } else biz
                    }
                    h.copy(subsidiaries = newSubs)
                }
            }
            
            val updatedHolding = updatedHoldings.find { it.instanceId == holdingId }?.copy(
                subsidiaries = (updatedHoldings.find { it.instanceId == holdingId }?.subsidiaries ?: emptyList()) + newOwnedBusiness
            )
            
            if (updatedHolding != null) {
                updatedHoldings = updatedHoldings.map { if (it.instanceId == holdingId) updatedHolding else it }
            }
            
            _playerState.value = currentState.copy(
                cash = currentState.cash - catalogItem.costToBuy,
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
            saveState(_playerState.value)
        }
    }

    fun buyBusiness(businessId: String, customName: String? = null, studioType: String = "LIVE_ACTION", vendorId: String? = null, firstHub: String? = null, parentId: String? = null) {
        val currentState = _playerState.value
        val catalogItem = getCatalogItem(businessId, currentState) ?: return

        val slotsUsed = currentState.ownedBusinesses.size + currentState.holdingCompanies.size
        if (slotsUsed >= currentState.maxBusinessSlots) return

        if (currentState.cash >= catalogItem.costToBuy) {
            var newOwned = com.example.data.OwnedBusiness(
                catalogId = businessId,
                customName = customName,
                level = 1,
                purchasedUpgrades = emptySet(),
                studioType = studioType,
                airlineHubs = if (firstHub != null) listOf(firstHub) else emptyList(),
                parentId = parentId
            )
            
            var updatedBusinesses = currentState.ownedBusinesses
            var updatedHoldings = currentState.holdingCompanies
            
            if (vendorId != null) {
                val profitForConstruction = (catalogItem.costToBuy * 0.40)
                newOwned = newOwned.copy(
                    isUpgrading = true,
                    upgradeDelayMonths = 3
                )
                
                updatedBusinesses = updatedBusinesses.map { biz ->
                    if (biz.instanceId == vendorId) {
                        val newTender = com.example.data.ConstructionProject(
                            name = "Internal: ${customName ?: catalogItem.name}",
                            totalContractValue = profitForConstruction,
                            durationMonths = 3,
                            remainingMonths = 3
                        )
                        biz.copy(
                            companyCash = biz.companyCash + profitForConstruction.toLong(),
                            activeTenders = biz.activeTenders + newTender
                        )
                    } else biz
                }
                
                updatedHoldings = updatedHoldings.map { h ->
                    val newSubs = h.subsidiaries.map { biz ->
                        if (biz.instanceId == vendorId) {
                            val newTender = com.example.data.ConstructionProject(
                                name = "Internal: ${customName ?: catalogItem.name}",
                                totalContractValue = profitForConstruction,
                                durationMonths = 3,
                                remainingMonths = 3
                            )
                            biz.copy(
                                companyCash = biz.companyCash + profitForConstruction.toLong(),
                                activeTenders = biz.activeTenders + newTender
                            )
                        } else biz
                    }
                    h.copy(subsidiaries = newSubs)
                }
            }
            
            updatedBusinesses = updatedBusinesses + newOwned

            _playerState.value = currentState.copy(
                cash = currentState.cash - catalogItem.costToBuy,
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        }
        saveState(_playerState.value)
    }

    fun acquireAircraft(businessId: String, type: String, name: String, isNew: Boolean, cost: Long, capacity: Int, maintenanceCost: Double) {
        val currentState = _playerState.value
        if (currentState.cash < cost) return
        
        val newAircraft = com.example.data.Aircraft(
            type = type,
            name = name,
            capacity = capacity,
            isUsed = !isNew,
            condition = if (isNew) 100 else (60..85).random(),
            maintenanceCost = maintenanceCost,
            deliveryDelay = if (isNew) (1..3).random() else 0
        )
        
        // Find if it's in owned businesses
        var updated = false
        val newBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == businessId) {
                updated = true
                biz.copy(airlineFleet = biz.airlineFleet + newAircraft)
            } else biz
        }
        
        if (updated) {
            _playerState.value = currentState.copy(cash = currentState.cash - cost, ownedBusinesses = newBusinesses)
            saveState(_playerState.value)
            return
        }
        
        // Otherwise, it might be inside a holding company
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { biz ->
                if (biz.instanceId == businessId) {
                    updated = true
                    biz.copy(airlineFleet = biz.airlineFleet + newAircraft)
                } else biz
            }
            holding.copy(subsidiaries = newSubs)
        }
        
        if (updated) {
             _playerState.value = currentState.copy(cash = currentState.cash - cost, holdingCompanies = newHoldings)
             saveState(_playerState.value)
        }
    }

    fun updateBusiness(businessId: String, cost: Long = 0L, mapper: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness) {
        val currentState = _playerState.value
        if (currentState.cash < cost) return
        
        var updated = false
        val newBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == businessId) {
                updated = true
                mapper(biz)
            } else biz
        }
        
        if (updated) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                ownedBusinesses = newBusinesses
            )
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { biz ->
                if (biz.instanceId == businessId) {
                    updated = true
                    mapper(biz)
                } else biz
            }
            holding.copy(subsidiaries = newSubs)
        }
        
        if (updated) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                holdingCompanies = newHoldings
            )
            saveState(_playerState.value)
        }
    }

    fun buyAviationHubComplex(businessId: String, city: String, cost: Long, buildTime: Int = 0) {
        updateBusiness(businessId, cost = 0L) { biz ->
            val finalCost = if (biz.companyCash >= cost) cost else 0L
            val newHub = com.example.data.AviationHub(
                city = city,
                baseCost = cost,
                activeUpgrades = emptyList(),
                constructionQueue = emptyList(),
                isConstructing = buildTime > 0,
                constructionMonthsLeft = buildTime
            )
            biz.copy(
                companyCash = biz.companyCash - finalCost,
                airlineHubsComplex = biz.airlineHubsComplex + newHub,
                airlineHubs = biz.airlineHubs + city
            )
        }
    }

    fun startHubUpgradeComplex(businessId: String, hubId: String, upgradeId: String, cost: Long, buildTime: Int) {
        updateBusiness(businessId, cost = cost) { biz ->
            val updatedHubs = biz.airlineHubsComplex.map { hub ->
                if (hub.id == hubId) {
                    val newQueueItem = com.example.data.HubConstructionItem(upgradeId, buildTime)
                    hub.copy(constructionQueue = hub.constructionQueue + newQueueItem)
                } else hub
            }
            biz.copy(airlineHubsComplex = updatedHubs)
        }
    }

    fun buyComplexAircraft(businessId: String, modelId: String, cost: Long, deliveryTime: Int, isLeased: Boolean = false, leasePrice: Long = 0L, quantity: Int = 1) {
        val actualCost = if (isLeased) 0L else (cost * quantity)
        updateBusiness(businessId, cost = 0L) { biz ->
            val newPlanes = (1..quantity).map {
                com.example.data.AircraftInstance(
                    id = java.util.UUID.randomUUID().toString(),
                    modelId = modelId,
                    condition = 100.0,
                    status = "DELIVERING",
                    monthsUntilDelivery = deliveryTime,
                    stationedHubId = null,
                    assignedRouteId = null,
                    isLeased = isLeased,
                    leasePrice = leasePrice
                )
            }
            biz.copy(
                companyCash = biz.companyCash - actualCost,
                airlineFleetComplex = biz.airlineFleetComplex + newPlanes
            )
        }
    }

    fun assignAircraftToHubComplex(businessId: String, aircraftId: String, hubId: String?) {
        updateBusiness(businessId) { biz ->
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.id == aircraftId) {
                    plane.copy(
                        stationedHubId = hubId,
                        status = if (hubId == null) "STANDBY" else plane.status,
                        assignedRouteId = null
                    )
                } else plane
            }
            val updatedRoutes = biz.flightRoutes.map { route ->
                if (route.assignedAircraftIds.contains(aircraftId)) {
                    route.copy(assignedAircraftIds = route.assignedAircraftIds - aircraftId)
                } else route
            }
            biz.copy(airlineFleetComplex = updatedFleet, flightRoutes = updatedRoutes)
        }
    }

    fun createFlightRouteComplex(businessId: String, originHubId: String, destination: String, distanceCategory: String, demand: Int, ticketPrice: Int) {
        updateBusiness(businessId) { biz ->
            val newRoute = com.example.data.FlightRoute(
                originHubId = originHubId,
                destination = destination,
                distanceCategory = distanceCategory,
                baseDemand = demand,
                ticketPrice = ticketPrice,
                assignedAircraftIds = emptyList()
            )
            biz.copy(flightRoutes = biz.flightRoutes + newRoute)
        }
    }

    fun deleteFlightRouteComplex(businessId: String, routeId: String) {
        updateBusiness(businessId) { biz ->
            val updatedRoutes = biz.flightRoutes.filter { it.id != routeId }
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.assignedRouteId == routeId) {
                    plane.copy(assignedRouteId = null, status = "STANDBY")
                } else plane
            }
            biz.copy(flightRoutes = updatedRoutes, airlineFleetComplex = updatedFleet)
        }
    }

    fun assignAircraftToRouteComplex(businessId: String, aircraftId: String, routeId: String?) {
        updateBusiness(businessId) { biz ->
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.id == aircraftId) {
                    plane.copy(
                        assignedRouteId = routeId,
                        status = if (routeId != null) "ASSIGNED" else "STANDBY"
                    )
                } else plane
            }
            val updatedRoutes = biz.flightRoutes.map { route ->
                val alreadyAssigned = route.assignedAircraftIds.contains(aircraftId)
                if (route.id == routeId) {
                    if (!alreadyAssigned) {
                        route.copy(assignedAircraftIds = route.assignedAircraftIds + aircraftId)
                    } else route
                } else {
                    if (alreadyAssigned) {
                        route.copy(assignedAircraftIds = route.assignedAircraftIds - aircraftId)
                    } else route
                }
            }
            biz.copy(airlineFleetComplex = updatedFleet, flightRoutes = updatedRoutes)
        }
    }

    fun calculateAviationExpenses(owned: com.example.data.OwnedBusiness): Long {
        var totalExp = 0L
        owned.airlineFleetComplex.forEach { plane ->
            if (plane.status != "DELIVERING") {
                val pDef = com.example.data.AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                    ?: com.example.data.DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                val baseUpkeep = if (pDef != null) {
                    pDef.price * 0.005
                } else {
                    when (plane.modelId) {
                        "atr72" -> 100000.0
                        "a320" -> 250000.0
                        "b777" -> 600000.0
                        else -> 150000.0
                    }
                }
                val finalUpkeep = baseUpkeep * (1.5 - plane.condition / 200.0)
                totalExp += finalUpkeep.toLong()
            }
            if (plane.isLeased) {
                totalExp += plane.leasePrice
            }
        }
        owned.airlineHubsComplex.forEach { hub ->
            var hubUpkeep = 100000L // Base upkeep
            hub.activeUpgrades.forEach { upgId ->
                val addCost = when (upgId) {
                    "upg_dom" -> 50000L
                    "upg_intl_1" -> 150000L
                    "upg_vip" -> 100000L
                    "upg_intl_2" -> 300000L
                    "upg_cargo" -> 120000L
                    else -> 50000L
                }
                hubUpkeep += addCost
            }
            totalExp += hubUpkeep
        }
        return totalExp
    }

    fun processAviationMonthlyTick(owned: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        val updatedFleet = owned.airlineFleetComplex.map { plane ->
            if (plane.status == "DELIVERING") {
                val remain = plane.monthsUntilDelivery - 1
                if (remain <= 0) {
                    plane.copy(status = "STANDBY", monthsUntilDelivery = 0)
                } else {
                    plane.copy(monthsUntilDelivery = remain)
                }
            } else {
                val activeRoute = owned.flightRoutes.find { r -> r.assignedAircraftIds.contains(plane.id) }
                val degradeRate = if (activeRoute != null) 2.0 else 0.5
                val newCond = (plane.condition - degradeRate).coerceAtLeast(0.0)
                plane.copy(condition = newCond)
            }
        }

        val updatedHubs = owned.airlineHubsComplex.map { hub ->
            var nextIsConstructing = hub.isConstructing
            var nextMonthsLeft = hub.constructionMonthsLeft
            if (hub.isConstructing) {
                val rem = hub.constructionMonthsLeft - 1
                if (rem <= 0) {
                    nextIsConstructing = false
                    nextMonthsLeft = 0
                } else {
                    nextMonthsLeft = rem
                }
            }

            val completedUpgrades = hub.activeUpgrades.toMutableList()
            val nextQueue = mutableListOf<com.example.data.HubConstructionItem>()
            hub.constructionQueue.forEach { item ->
                val remain = item.monthsRemaining - 1
                if (remain <= 0) {
                    completedUpgrades.add(item.upgradeId)
                } else {
                    nextQueue.add(item.copy(monthsRemaining = remain))
                }
            }
            hub.copy(
                activeUpgrades = completedUpgrades,
                constructionQueue = nextQueue,
                isConstructing = nextIsConstructing,
                constructionMonthsLeft = nextMonthsLeft
            )
        }

        var totalRev = 0L
        owned.flightRoutes.forEach { route ->
            val participatingPlanes = updatedFleet.filter { plane -> 
                route.assignedAircraftIds.contains(plane.id) && plane.status != "DELIVERING" 
            }
            if (participatingPlanes.isNotEmpty()) {
                val totalCapacityOnRoute = participatingPlanes.sumOf { plane ->
                    val pDef = com.example.data.AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                        ?: com.example.data.DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                    val maxCap = pDef?.maxPax ?: when (plane.modelId) {
                        "atr72" -> 72
                        "a320" -> 180
                        "b777" -> 350
                        else -> 150
                    }
                    (maxCap * (0.3 + 0.7 * (plane.condition / 100.0))).toInt()
                }
                if (totalCapacityOnRoute > 0) {
                    val dailyPax = kotlin.math.min(route.baseDemand, totalCapacityOnRoute)
                    val monthlyPax = dailyPax * 30L
                    val routeRevenue = monthlyPax * route.ticketPrice
                    totalRev += routeRevenue
                }
            }
        }

        return owned.copy(
            airlineFleetComplex = updatedFleet,
            airlineHubsComplex = updatedHubs,
            customRevenue = totalRev
        )
    }

    fun processCruiseMonthlyTick(owned: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        var currentPrestige = owned.cruiseBrandPrestige
        val updatedShips = owned.cruiseShips?.map { ship ->
            com.example.viewmodel.CruiseEngine.processShipMonthly(ship, currentPrestige) { np ->
                currentPrestige = np
            }
        } ?: emptyList()
        return owned.copy(
            cruiseShips = updatedShips,
            cruiseBrandPrestige = currentPrestige
        )
    }

    fun calculateCruiseNetProfit(owned: com.example.data.OwnedBusiness): Long {
        val catalogItem = getCatalogItem(owned.catalogId, _playerState.value) ?: return 0L
        val totalRev = owned.cruiseShips?.sumOf { it.lastMonthTicketRevenue + it.lastMonthOnboardRevenue } ?: 0L
        val totalExp = (owned.cruiseShips?.sumOf { it.lastMonthExpenses } ?: 0L) + catalogItem.monthlyMaintenanceCost
        return totalRev - totalExp
    }

    fun orderCruiseShip(
        businessId: String,
        name: String,
        shipClass: com.example.data.CruiseShipClass,
        shipyard: com.example.data.ShipyardId
    ): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            val finalPrice = (shipClass.basePrice * (1.0 + shipyard.costModifier)).toLong()
            if (owned.companyCash >= finalPrice) {
                success = true
                val totalBuild = (shipClass.baseBuildTime - shipyard.buildTimeReduction).coerceAtLeast(3)
                val newShip = com.example.data.CruiseShip(
                    name = name,
                    shipClass = shipClass,
                    shipyard = shipyard,
                    maxPax = shipClass.maxPax,
                    pricePaid = finalPrice,
                    monthsUntilDelivery = totalBuild,
                    totalBuildTime = totalBuild
                )
                owned.copy(
                    companyCash = owned.companyCash - finalPrice,
                    cruiseShips = (owned.cruiseShips ?: emptyList()) + newShip
                )
            } else {
                owned
            }
        }
        return success
    }

    fun renameCruiseShip(businessId: String, shipId: String, newName: String, customImageUrl: String?) {
        updateBusiness(businessId) { owned ->
            val updated = (owned.cruiseShips ?: emptyList()).map { ship ->
                if (ship.id == shipId) {
                    ship.copy(name = newName, customImageUrl = customImageUrl)
                } else ship
            }
            owned.copy(cruiseShips = updated)
        }
    }

    fun scrapCruiseShip(businessId: String, shipId: String) {
        updateBusiness(businessId) { owned ->
            val targetShip = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (targetShip != null) {
                val refund = (targetShip.pricePaid * 0.15).toLong()
                val updatedShips = (owned.cruiseShips ?: emptyList()).filter { it.id != shipId }
                owned.copy(
                    companyCash = owned.companyCash + refund,
                    cruiseShips = updatedShips
                )
            } else owned
        }
    }

    fun assignCruiseShipPort(businessId: String, shipId: String, portId: String?) {
        updateBusiness(businessId) { owned ->
            val updated = (owned.cruiseShips ?: emptyList()).map { ship ->
                if (ship.id == shipId) {
                    ship.copy(assignedPortId = portId)
                } else ship
            }
            owned.copy(cruiseShips = updated)
        }
    }

    fun buyCruiseFacility(businessId: String, shipId: String, facilityId: String): Boolean {
        var success = false
        val facility = com.example.data.CRUISE_FACILITIES_CATALOG.find { it.id == facilityId } ?: return false
        updateBusiness(businessId) { owned ->
            val ship = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (ship != null && owned.companyCash >= facility.cost && !ship.builtFacilities.contains(facilityId)) {
                success = true
                val updatedShips = owned.cruiseShips.map { s ->
                    if (s.id == shipId) {
                        s.copy(builtFacilities = s.builtFacilities + facilityId)
                    } else s
                }
                owned.copy(
                    companyCash = owned.companyCash - facility.cost,
                    cruiseShips = updatedShips
                )
            } else {
                owned
            }
        }
        return success
    }

    fun sendCruiseShipToDrydock(businessId: String, shipId: String): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            val ship = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (ship != null && !ship.isUnderDrydock && ship.monthsUntilDelivery == 0) {
                success = true
                val updatedShips = owned.cruiseShips.map { s ->
                    if (s.id == shipId) {
                        s.copy(
                            isUnderDrydock = true,
                            drydockMonthsRemaining = 2,
                            lastMonthExpenses = (s.pricePaid * 0.02).toLong()
                        )
                    } else s
                }
                owned.copy(
                    cruiseShips = updatedShips
                )
            } else {
                owned
            }
        }
        return success
    }

    fun unlockCruisePort(businessId: String, portId: String, cost: Long): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            if (owned.companyCash >= cost && !(owned.cruisePortsUnlocked ?: emptyList()).contains(portId)) {
                success = true
                owned.copy(
                    companyCash = owned.companyCash - cost,
                    cruisePortsUnlocked = (owned.cruisePortsUnlocked ?: emptyList()) + portId
                )
            } else owned
        }
        return success
    }

    fun buildHotelProperty(instanceId: String, name: String, location: String, tier: com.example.data.HotelTier) {
        val currentState = _playerState.value
        val cost = tier.baseBuildCost
        
        var foundInOwned = true
        var targetIndex = currentState.ownedBusinesses.indexOfFirst { it.instanceId == instanceId }
        var targetBusiness: com.example.data.OwnedBusiness? = null
        
        if (targetIndex != -1) {
            targetBusiness = currentState.ownedBusinesses[targetIndex]
        } else {
            foundInOwned = false
            for (holding in currentState.holdingCompanies) {
                targetIndex = holding.subsidiaries.indexOfFirst { it.instanceId == instanceId }
                if (targetIndex != -1) {
                    targetBusiness = holding.subsidiaries[targetIndex]
                    // We also need to get the holding company index to update it
                    break
                }
            }
        }
        
        if (targetBusiness == null) return
        
        if (targetBusiness.companyCash >= cost) {
            val newProp = com.example.data.HotelProperty(
                name = name,
                location = location,
                tier = tier,
                isConstructing = true,
                remainingBuildMonths = tier.buildMonths,
                customRoomRate = tier.baseRoomRate,
                builtFacilities = mutableListOf()
            )
            
            val updatedHospitality = targetBusiness.hospitalityProperties + newProp
            val updatedBusiness = targetBusiness.copy(
                companyCash = targetBusiness.companyCash - cost,
                hospitalityProperties = updatedHospitality
            )
            
            if (foundInOwned) {
                val newList = currentState.ownedBusinesses.toMutableList()
                newList[currentState.ownedBusinesses.indexOfFirst { it.instanceId == instanceId }] = updatedBusiness
                _playerState.value = currentState.copy(ownedBusinesses = newList)
            } else {
                val newHoldings = currentState.holdingCompanies.toMutableList()
                val holdingIdx = newHoldings.indexOfFirst { it.subsidiaries.any { s -> s.instanceId == instanceId } }
                if (holdingIdx != -1) {
                    val holding = newHoldings[holdingIdx]
                    val subIdx = holding.subsidiaries.indexOfFirst { it.instanceId == instanceId }
                    val newSubs = holding.subsidiaries.toMutableList()
                    newSubs[subIdx] = updatedBusiness
                    newHoldings[holdingIdx] = holding.copy(subsidiaries = newSubs)
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                }
            }
            saveState(_playerState.value)
        }
    }

    fun updateHotelImage(instanceId: String, hotelId: String, imageUrl: String) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(imageUrl = imageUrl) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateHotelRoomStrategy(instanceId: String, hotelId: String, strategy: String) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(targetRoomStrategy = strategy) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateRoomClassConfig(instanceId: String, hotelId: String, roomClassName: String, config: com.example.data.RoomClassConfig) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) {
                            val newConfigs = h.roomConfigs?.toMutableMap() ?: mutableMapOf()
                            newConfigs[roomClassName] = config
                            h.copy(roomConfigs = newConfigs)
                        } else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateHotelRoomRate(instanceId: String, hotelId: String, newRate: Long) {
        val currentState = _playerState.value
        
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(customRoomRate = newRate) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            if (subChanged) holding.copy(subsidiaries = newSubs) else holding
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun buildHotelFacility(instanceId: String, hotelId: String, facility: com.example.data.HotelFacility) {
        val currentState = _playerState.value
        val cost = facility.buildCost
        
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId && bus.companyCash >= cost) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId && !h.builtFacilities.contains(facility)) {
                            val bf = h.builtFacilities.toMutableList()
                            bf.add(facility)
                            changed = true
                            h.copy(builtFacilities = bf)
                        } else h
                    }
                    if (changed) {
                        bus.copy(companyCash = bus.companyCash - cost, hospitalityProperties = ph)
                    } else bus
                } else bus
            }
            return Pair(newList, changed)
        }
        
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            if (subChanged) holding.copy(subsidiaries = newSubs) else holding
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }
    fun addDivisionToAcquiredBusiness(parentInstanceId: String, catalogId: String, customName: String?, cost: Long): String? {
        val currentState = _playerState.value
        val parent = currentState.ownedBusinesses.find { it.instanceId == parentInstanceId }
        if (parent == null) {
            return "Perusahaan induk tidak ditemukan."
        }
        if (parent.acquiredStockTicker == null) {
            return "Perusahaan induk bukan perusahaan hasil akuisisi publik."
        }
        if (parent.companyCash < cost) {
            val neededStr = com.example.ui.formatCurrencyRingkas(cost, false)
            val currentStr = com.example.ui.formatCurrencyRingkas(parent.companyCash.toLong(), false)
            return "Kas internal perusahaan tidak mencukupi (Butuh $neededStr, Kas: $currentStr). Silakan lakukan suntik dana terlebih dahulu."
        }
        
        val newDivision = com.example.data.OwnedBusiness(
            catalogId = catalogId,
            customName = customName,
            level = 1,
            companyCash = 0.0
        )
        
        val updatedParent = parent.copy(
            companyCash = parent.companyCash - cost,
            subsidiaries = parent.subsidiaries + newDivision
        )
        
        val updatedOwnedList = currentState.ownedBusinesses.map {
            if (it.instanceId == parentInstanceId) updatedParent else it
        }
        
        _playerState.value = currentState.copy(
            ownedBusinesses = updatedOwnedList
        )
        saveState(_playerState.value)
        return null
    }

    fun purchaseUpgrade(instanceId: String, upgradeId: String) {
        val currentState = _playerState.value
        
        // Find if it's in regular businesses
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            // Check in holdings
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return

        val catalogItem = getCatalogItem(owned.catalogId, currentState) ?: return
        val upgrade = catalogItem.upgrades.find { it.id == upgradeId } ?: return

        val currentLevel = owned.upgradeLevels[upgradeId] ?: if (owned.purchasedUpgrades.contains(upgradeId)) 1 else 0
        if (currentLevel >= upgrade.maxLevel) return

        var costMultiplierTotal = 1.0f
        repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
        val cost = (upgrade.baseCost * costMultiplierTotal).toLong()

        if (owned.companyCash >= cost) {
            val durationMs = (60000L * Math.pow(1.3, currentLevel.toDouble())).toLong()
            val now = System.currentTimeMillis()
            
            val newActiveUpgrade = ActiveUpgrade(
                selectedUpgradeId = upgradeId,
                targetLevel = currentLevel + 1,
                startTimeMs = now,
                finishTimeMs = now + durationMs
            )

            val newOwned = owned.copy(
                activeUpgrades = owned.activeUpgrades + newActiveUpgrade,
                companyCash = owned.companyCash - cost
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { 
                            if (it.instanceId == instanceId) newOwned else it 
                        }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(
                    holdingCompanies = newHoldings
                )
            } else {
                _playerState.value = currentState.copy(
                    ownedBusinesses = currentState.ownedBusinesses.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                )
            }
            saveState(_playerState.value)
        }
    }

    fun startParentBusinessRealtimeUpgrade(instanceId: String, cost: Long) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        if (owned.level >= 50 || owned.isUpgradingRealTime) return
        
        if (owned.companyCash >= cost) {
            val durationInSeconds = 30 + (owned.level * 12)
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                isUpgradingRealTime = true,
                upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L)
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { 
                            if (it.instanceId == instanceId) newOwned else it 
                        }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(
                    holdingCompanies = newHoldings
                )
            } else {
                _playerState.value = currentState.copy(
                    ownedBusinesses = currentState.ownedBusinesses.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                )
            }
            saveState(_playerState.value)
        }
    }

    fun finishBusinessRealtimeUpgrade(instanceId: String) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        if (!owned.isUpgradingRealTime) return
        
        val newOwned = owned.copy(
            level = owned.level + 1,
            isUpgradingRealTime = false,
            upgradeEndTimeRealTime = 0L
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
    }

    fun produceMovie(instanceId: String, title: String, budget: Long, promoBudget: Long, genres: List<String>, isGlobal: Boolean, schedMonth: Int? = null, schedYear: Int? = null, filmFormat: String = "Feature Film", productionFocus: String = "REGULER"): Boolean {
        val currentState = _playerState.value
        val totalCost = budget + promoBudget

        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return false
        
        if (owned.companyCash < totalCost) return false

        // Anti-Duplicate checking
        val cleanTitle = title.trim()
        if (owned.projectHistory.any { it.title.trim().equals(cleanTitle, ignoreCase = true) }) {
            return false // duplicate title
        }

        // RNG Logic manipulated by Production Focus
        var reviewScore = when (productionFocus) {
            "KUALITAS" -> (65..100).random()
            "MAHAKARYA" -> (85..100).random()
            else -> {
                val lowerBound = minOf(10 + (owned.level * 2), 70)
                var score = (lowerBound..100).random()
                
                // High volatility for 3+ genres
                if (genres.size >= 3) {
                    if ((0..1).random() == 0) {
                        score -= (10..30).random()
                    } else {
                        score += (10..20).random()
                    }
                    score = score.coerceIn(0, 100)
                }
                score
            }
        }

        val distMult = if (isGlobal) 2.5 else 1.0
        val levelBonus = 1.0 + (owned.level * 0.05)
        
        val totalInvestment = budget + promoBudget
        val promoRatio = if (budget > 0) promoBudget.toDouble() / budget.toDouble() else 0.0
        val promoMult = 1.0 + (promoRatio * 0.5).coerceAtMost(2.0)
        val viralMult = if (reviewScore > 85) 1.5 + ((reviewScore - 85) * 0.1) else 1.0

        val animQualityMult = if (owned.studioType == "ANIMATION" && budget > 50000000) 1.5 else 1.0

        var boxOffice = if (reviewScore < 40) {
            (totalInvestment * (0.1 + (promoRatio * 0.1).coerceAtMost(0.4))).toLong()
        } else if (reviewScore > 85 && isGlobal) {
            (totalInvestment * (5..10).random() * levelBonus * promoMult * viralMult * animQualityMult).toLong() 
        } else {
            val performance = reviewScore / 100.0
            (totalInvestment * performance * distMult * levelBonus * 3.0 * promoMult * viralMult * animQualityMult).toLong()
        }

        val netProfit = boxOffice - totalInvestment
        val initMonths = if (isGlobal) 6 else 4
        
        val baseDelay = when {
            owned.studioType == "ANIMATION" && filmFormat == "Short Film" -> (14..24).random()
            owned.studioType == "ANIMATION" && (filmFormat == "Feature Film" || filmFormat.isBlank()) -> (24..60).random()
            owned.studioType == "LIVE_ACTION" && filmFormat == "Short Film" -> (3..5).random()
            else -> (12..24).random() // Live-Action Feature
        }
        val budgetInTenMillions = (budget / 10000000).toInt()
        val penalty = minOf(budgetInTenMillions, 12)
        val focusPenalty = when (productionFocus) {
            "KUALITAS" -> 6
            "MAHAKARYA" -> 12
            else -> 0
        }
        val delayMonths = baseDelay + penalty + focusPenalty

        val project = com.example.data.MovieProject(
            title = cleanTitle,
            budget = budget,
            genres = genres,
            distributionScale = if (isGlobal) "Global" else "Local",
            reviewScore = reviewScore,
            boxOffice = boxOffice, // Retained for compatibility / reference
            netProfit = netProfit,
            status = "IN_PRODUCTION",
            remainingMonths = initMonths,
            currentRevenue = 0L,
            targetMaxRevenue = boxOffice,
            productionPhase = if (schedMonth != null && schedYear != null && (schedYear > currentState.inGameYear || (schedYear == currentState.inGameYear && schedMonth > currentState.inGameMonth))) "ANTREAN" else "Pra-Produksi",
            productionDelayMonths = delayMonths,
            promoBudget = promoBudget,
            scheduledMonth = schedMonth,
            scheduledYear = schedYear,
            filmFormat = filmFormat,
            productionFocus = productionFocus
        )

        val newOwned = owned.copy(
            projectHistory = owned.projectHistory + project,
            companyCash = owned.companyCash - totalCost
        )

        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
        return true
    }

    fun cancelMovieProject(instanceId: String, projectTitle: String, refundAmount: Long) {
        val currentState = _playerState.value
        
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        
        val newProjList = owned.projectHistory.filter { it.title != projectTitle }
        
        val newOwned = owned.copy(
            projectHistory = newProjList,
            companyCash = owned.companyCash + refundAmount
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
    }

    fun polishMovieProject(instanceId: String, projectTitle: String, budgetCost: Long, extraMonths: Int) {
        val currentState = _playerState.value
        
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        
        val fromCompanyCash = owned.companyCash >= budgetCost
        if (!fromCompanyCash && currentState.cash < budgetCost) return
        
        val newProjList = owned.projectHistory.map { proj ->
            if (proj.title == projectTitle && proj.isQcPhase) {
                val scoreBump = (5..20).random()
                val score = (proj.internalScore ?: proj.reviewScore) + scoreBump
                proj.copy(
                    isQcPhase = false,
                    productionPhase = "Poles Visual",
                    productionDelayMonths = proj.productionDelayMonths + extraMonths,
                    internalScore = score.coerceIn(1, 99)
                )
            } else proj
        }
        
        val newOwned = owned.copy(
            projectHistory = newProjList,
            companyCash = if(fromCompanyCash) owned.companyCash - budgetCost else owned.companyCash
        )
        
        val newPlayerCash = if(!fromCompanyCash) currentState.cash - budgetCost else currentState.cash

        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings, cash = newPlayerCash)
        } else {
            _playerState.value = currentState.copy(
                cash = newPlayerCash,
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
    }

    fun scheduleMovieRelease(instanceId: String, projectTitle: String, schedStr: String) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        
        val newProjList = owned.projectHistory.map { proj ->
            if (proj.title == projectTitle && proj.isQcPhase) {
                proj.copy(
                    isQcPhase = false,
                    isAwaitingRelease = true,
                    scheduledReleaseDate = schedStr
                )
            } else proj
        }
        
        val newOwned = owned.copy(projectHistory = newProjList)

        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
    }

    fun getBookedTimeSlots(): List<String> {
        return _playerState.value.activeTvPrograms.filter { it.active }.flatMap { it.timeSlots }
    }

    fun updateTvProgramSchedule(programId: String, newTimeSlots: List<String>) {
        val currentState = _playerState.value
        val updatedPrograms = currentState.activeTvPrograms.map {
            if (it.id == programId) it.copy(timeSlots = newTimeSlots) else it
        }
        _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms)
        saveState(_playerState.value)
    }

    fun addTvProgram(instanceId: String, title: String, type: String, productionCost: Double, isPremiumRights: Boolean = false, finalCost: Long = productionCost.toLong(), durationMonths: Int = -1, timeSlots: List<String> = emptyList()): Boolean {
        val currentState = _playerState.value
        
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        if (owned == null) return false
        
        if (currentState.activeTvPrograms.any { it.title.equals(title, ignoreCase = true) } || 
            currentState.ipLibraryHistory.any { it.title.equals(title, ignoreCase = true) }) {
            return false // duplicate
        }

        if (owned.companyCash >= finalCost) {
            val rating = if (isPremiumRights) {
                80.0 + (Math.random() * 15.0) // 80 - 95%
            } else {
                when (type) {
                    "Pencarian Bakat (Talent Show)" -> 40.0 + (Math.random() * 40.0) // 40 - 80% (High risk high reward)
                    "Investigasi Kriminal" -> Math.random() * 20.0 // 0 - 20%
                    "Sinetron" -> 10.0 + (Math.random() * 30.0) // 10 - 40%
                    "Reality Show" -> 5.0 + (Math.random() * 40.0)
                    else -> Math.random() * 40.0 // 0 - 40%
                }
            }
            
            // Calculate initial average multiplier from time slots
            val timeMuls = timeSlots.map { slot ->
                val hour = slot.substringBefore(":").toInt()
                val isHalfHour = slot.substringAfter(":") == "30"
                val minutes = hour * 60 + (if (isHalfHour) 30 else 0)
                
                if (minutes in 6 * 60..11 * 60 + 30) 1.0
                else if (minutes in 12 * 60..17 * 60 + 30) 0.6
                else if (minutes in 18 * 60..22 * 60 + 30) 2.5
                else 0.3
            }
            val avgMultiplier = if (timeMuls.isNotEmpty()) timeMuls.average() else 1.0

            val adRevenue = (productionCost * (rating / 10.0)) * avgMultiplier

            val newProgram = com.example.data.TvProgram(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                type = type,
                productionCost = productionCost,
                monthlyAdRevenue = adRevenue,
                rating = rating,
                active = true,
                remainingMonths = durationMonths,
                timeSlots = timeSlots
            )
            val newList = currentState.activeTvPrograms + newProgram
            
            val newOwned = owned.copy(companyCash = owned.companyCash - finalCost)

            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        holding.copy(subsidiaries = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it })
                    } else holding
                }
                _playerState.value = currentState.copy(
                    activeTvPrograms = newList,
                    holdingCompanies = newHoldings
                )
            } else {
                _playerState.value = currentState.copy(
                    activeTvPrograms = newList,
                    ownedBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
                )
            }

            _playerState.value = syncTvValuation(_playerState.value)
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun cancelTvProgram(programId: String) {
        val currentState = _playerState.value
        val progToCancel = currentState.activeTvPrograms.find { it.id == programId }
        val updatedPrograms = currentState.activeTvPrograms.filter { it.id != programId }
        
        if (progToCancel != null && progToCancel.isOriginalIP) {
            val archivedProg = progToCancel.copy(active = false, remainingMonths = 0)
            val newIpLibrary = currentState.ipLibraryHistory + archivedProg
            _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms, ipLibraryHistory = newIpLibrary)
        } else {
            _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms)
        }
        
        _playerState.value = syncTvValuation(_playerState.value)
        saveState(_playerState.value)
    }

    fun rebootTvProgram(programId: String) {
        val currentState = _playerState.value
        val archivedProg = currentState.ipLibraryHistory.find { it.id == programId } ?: return
        
        val rebootCost = (archivedProg.productionCost * 2).toLong()
        if (currentState.cash >= rebootCost) {
            val newRating = when (archivedProg.type) {
                "Pencarian Bakat (Talent Show)" -> 40.0 + (Math.random() * 40.0) 
                "Investigasi Kriminal" -> Math.random() * 20.0
                "Sinetron" -> 10.0 + (Math.random() * 30.0) 
                "Reality Show" -> 5.0 + (Math.random() * 40.0)
                else -> Math.random() * 40.0 
            }
            val newAdRevenue = archivedProg.productionCost * (newRating / 10.0)
            
            val rebootedProg = archivedProg.copy(
                active = true,
                rating = newRating,
                monthlyAdRevenue = newAdRevenue,
                totalAccumulatedProfit = 0.0,
                monthsAired = 0,
                remainingMonths = -1
            )
            
            val updatedIpLibrary = currentState.ipLibraryHistory.filter { it.id != programId }
            val updatedActive = currentState.activeTvPrograms + rebootedProg
            
            _playerState.value = currentState.copy(
                cash = currentState.cash - rebootCost,
                ipLibraryHistory = updatedIpLibrary,
                activeTvPrograms = updatedActive
            )
            _playerState.value = syncTvValuation(_playerState.value)
            saveState(_playerState.value)
        }
    }

    fun startStreamingLicense(instanceId: String, title: String, licenseeName: String, fee: Long, duration: Int) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        
        val newHistory = owned.projectHistory.map { proj ->
            if (proj.title == title && proj.status == "FINISHED") {
                proj.copy(
                    licenseeName = licenseeName,
                    licenseMonthlyFee = fee,
                    licenseRemainingMonths = duration
                )
            } else {
                proj
            }
        }
        
        val newOwned = owned.copy(projectHistory = newHistory)
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { h ->
                if (h.instanceId == holdingId) {
                    val newSubs = h.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    h.copy(subsidiaries = newSubs)
                } else h
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        } else {
            val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
        }
        
        _playerState.value = syncTvValuation(_playerState.value)
        saveState(_playerState.value)
    }

    fun sellMovieIp(instanceId: String, title: String, sellPrice: Long) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return
        
        val proj = owned.projectHistory.find { it.title == title && it.status == "FINISHED" } ?: return
        val valueToDeduct = maxOf(0L, proj.netProfit)
        
        val newHistory = owned.projectHistory.filter { it != proj }
        val newOwned = owned.copy(
            projectHistory = newHistory,
            extraValuation = maxOf(0L, owned.extraValuation - valueToDeduct)
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings,
                cash = currentState.cash + sellPrice
            )
        } else {
            val newBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            _playerState.value = currentState.copy(
                ownedBusinesses = newBusinesses,
                cash = currentState.cash + sellPrice
            )
        }
        saveState(_playerState.value)
    }

    fun sellTvIp(programId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val prog = currentState.ipLibraryHistory.find { it.id == programId }
        val ownedTv = currentState.ownedBusinesses.find { getCatalogItem(it.catalogId, currentState)?.id == "media_tv" }

        if (prog != null) {
            val updatedHistory = currentState.ipLibraryHistory.filter { it.id != programId }

            _playerState.value = currentState.copy(
                ipLibraryHistory = updatedHistory,
                cash = currentState.cash + sellPrice
            )
            _playerState.value = syncTvValuation(_playerState.value)
            saveState(_playerState.value)
        }
    }

    fun startAppProject(title: String, type: com.example.data.ProjectType, budgetCost: Double, targetRevenue: Double, devTimeMonths: Int, targetBusinessId: String? = null) {
        val currentState = _playerState.value
        val newProject = com.example.data.AppProject(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            type = type,
            budgetCost = budgetCost,
            targetRevenue = targetRevenue,
            devTimeMonths = devTimeMonths,
            targetBusinessId = targetBusinessId
        )
        // We do not deduct cash up front, it's deducted monthly as "budgetCost / devTimeMonths" 
        // which matches OP's "potong biaya per bulan" description.
        _playerState.value = currentState.copy(
            appProjects = currentState.appProjects + newProject
        )
        saveState(_playerState.value)
    }

    fun sellSaaSProject(projectId: String) {
        val currentState = _playerState.value
        val project = currentState.appProjects.find { it.id == projectId }
        if (project != null && project.type == com.example.data.ProjectType.INDEPENDENT_SAAS && project.status == com.example.data.ProjectStatus.MAINTENANCE) {
            val acquisitionValue = (project.targetRevenue * 50).toLong()
            _playerState.value = currentState.copy(
                cash = currentState.cash + acquisitionValue,
                appProjects = currentState.appProjects.filter { it.id != projectId }
            )
            saveState(_playerState.value)
        }
    }

    private fun syncTvValuation(currentState: PlayerState): PlayerState {
        try {
            var activeProgramSum = 0L
            currentState.activeTvPrograms?.forEach { prog ->
                activeProgramSum += prog.productionCost.toLong()
            }

            var ipLibrarySum = 0L
            currentState.ipLibraryHistory?.forEach { item ->
                ipLibrarySum += ((item.productionCost * 0.5) + item.totalAccumulatedProfit).toLong()
            }

            val totalExtraTvValuation = (activeProgramSum + ipLibrarySum).coerceAtLeast(0L)

            val updatedBusinesses = currentState.ownedBusinesses?.map { b ->
                val cat = getCatalogItem(b.catalogId, currentState)
                if (cat?.id == "media_tv" || cat?.category == com.example.data.BusinessCategory.ENTERTAINMENT) {
                    if (cat.id == "media_tv") {
                        b.copy(extraValuation = totalExtraTvValuation)
                    } else b
                } else b
            } ?: emptyList()

            val updatedHoldings = currentState.holdingCompanies?.map { holding ->
                val updatedSubs = holding.subsidiaries?.map { sub ->
                    val cat = getCatalogItem(sub.catalogId, currentState)
                    if (cat?.id == "media_tv") {
                        sub.copy(extraValuation = totalExtraTvValuation)
                    } else sub
                } ?: emptyList()
                holding.copy(subsidiaries = updatedSubs)
            } ?: emptyList()

            return currentState.copy(
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return currentState
        }
    }

    fun repairDataStructure() {
        try {
            val currentState = _playerState.value
            
            // Step 1: Patch Mega Holding (Prevent Null / Reset Format)
            @Suppress("SENSELESS_COMPARISON")
            val megaHolding = if (currentState.megaHolding != null) currentState.megaHolding else com.example.data.MegaHoldingState(isActive = false, ownershipPercentage = 100.0)

            // Step 2: Patch Businesses (TV & Film)
            @Suppress("SENSELESS_COMPARISON")
            val rawBusinesses = if (currentState.ownedBusinesses != null) currentState.ownedBusinesses else emptyList()
            val patchedBusinesses = rawBusinesses.map { business ->
                try {
                    val catalogItem = getCatalogItem(business.catalogId, currentState)
                    if (catalogItem?.category == com.example.data.BusinessCategory.ENTERTAINMENT && catalogItem.name.contains("Film")) {
                        // Patch Movie Projects
                        @Suppress("SENSELESS_COMPARISON")
                        val rawProjects = if (business.projectHistory != null) business.projectHistory else emptyList()
                        val patchedProjects = rawProjects.map { proj ->
                            @Suppress("SENSELESS_COMPARISON")
                            proj.copy(
                                status = if (proj.status != null) proj.status else "FINISHED",
                                remainingMonths = proj.remainingMonths ?: 0
                            )
                        }
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (business.studioType == null || business.studioType.isBlank()) "LIVE_ACTION" else business.studioType
                        business.copy(
                            projectHistory = patchedProjects,
                            studioType = type
                        )
                    } else {
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (business.studioType == null || business.studioType.isBlank()) "LIVE_ACTION" else business.studioType
                        business.copy(studioType = type)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    business
                }
            }

            // Patch TV Programs 
            @Suppress("SENSELESS_COMPARISON")
            val rawActiveTv = if (currentState.activeTvPrograms != null) currentState.activeTvPrograms else emptyList()
            val patchedActiveTv = rawActiveTv.map { prog ->
                try {
                    @Suppress("SENSELESS_COMPARISON")
                    prog.copy(
                        timeSlots = if (prog.timeSlots != null) prog.timeSlots else emptyList(),
                        isOriginalIP = if (prog.isOriginalIP != null) prog.isOriginalIP else true,
                        monthsAired = prog.monthsAired ?: 0,
                        totalAccumulatedProfit = prog.totalAccumulatedProfit ?: 0.0
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    prog
                }
            }
            
            @Suppress("SENSELESS_COMPARISON")
            val rawLibraryTv = if (currentState.ipLibraryHistory != null) currentState.ipLibraryHistory else emptyList()
            val patchedLibraryTv = rawLibraryTv.map { prog ->
                try {
                    @Suppress("SENSELESS_COMPARISON")
                    prog.copy(
                        timeSlots = if (prog.timeSlots != null) prog.timeSlots else emptyList(),
                        isOriginalIP = if (prog.isOriginalIP != null) prog.isOriginalIP else true,
                        monthsAired = prog.monthsAired ?: 0,
                        totalAccumulatedProfit = prog.totalAccumulatedProfit ?: 0.0
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    prog
                }
            }

            @Suppress("SENSELESS_COMPARISON")
            val rawHoldings = if (currentState.holdingCompanies != null) currentState.holdingCompanies else emptyList()
            val patchedHoldings = rawHoldings.map { holding ->
                val patchedSubs = holding.subsidiaries.map { sub ->
                    val catalogItem = getCatalogItem(sub.catalogId, currentState)
                    if (catalogItem?.category == com.example.data.BusinessCategory.ENTERTAINMENT && catalogItem.name.contains("Film")) {
                        @Suppress("SENSELESS_COMPARISON")
                        val rawProjects = if (sub.projectHistory != null) sub.projectHistory else emptyList()
                        val patchedProjects = rawProjects.map { proj ->
                            @Suppress("SENSELESS_COMPARISON")
                            proj.copy(
                                status = if (proj.status != null) proj.status else "FINISHED",
                                remainingMonths = proj.remainingMonths ?: 0
                            )
                        }
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (sub.studioType == null || sub.studioType.isBlank()) "LIVE_ACTION" else sub.studioType
                        sub.copy(projectHistory = patchedProjects, studioType = type)
                    } else {
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (sub.studioType == null || sub.studioType.isBlank()) "LIVE_ACTION" else sub.studioType
                        sub.copy(studioType = type)
                    }
                }
                holding.copy(subsidiaries = patchedSubs)
            }

            val tempState = currentState.copy(
                megaHolding = megaHolding,
                ownedBusinesses = patchedBusinesses,
                holdingCompanies = patchedHoldings,
                activeTvPrograms = patchedActiveTv,
                ipLibraryHistory = patchedLibraryTv
            )

            // Step 3: Trigger a dummy payday cycle to recalculate netWorth safely locally, but without advancing the month.
            // For simplicity and safety, we just recalculate netWorth manually right here so we don't accidentally run side-effects.
            var businessValue = tempState.ownedBusinesses.sumOf {
                if (it.acquiredStockTicker != null) {
                    val stockInPortfolio = tempState.ownedStocks.find { s -> s.ticker == it.acquiredStockTicker }
                    val livePrice = _stockList.value.find { s -> s.ticker == it.acquiredStockTicker }?.currentPrice ?: stockInPortfolio?.averagePrice ?: 0.0
                    val baseVal = if (stockInPortfolio != null) {
                        (stockInPortfolio.shares * livePrice).toLong()
                    } else 0L
                    val subsidiariesVal = it.subsidiaries.sumOf { sub ->
                        val catItem = getCatalogItem(sub.catalogId, tempState)
                        if (catItem != null) getBusinessValuation(sub, catItem) else 0L
                    }
                    baseVal + it.companyCash.toLong() + subsidiariesVal
                } else {
                    val cat = getCatalogItem(it.catalogId, tempState)
                    if (cat != null) getBusinessValuation(it, cat) else 0L
                }
            }
            businessValue += tempState.holdingCompanies.sumOf { h -> 
                h.subsidiaries.sumOf { s -> 
                    if (s.acquiredStockTicker != null) {
                        val stockInPortfolio = tempState.ownedStocks.find { st -> st.ticker == s.acquiredStockTicker }
                        val livePrice = _stockList.value.find { st -> st.ticker == s.acquiredStockTicker }?.currentPrice ?: stockInPortfolio?.averagePrice ?: 0.0
                        val baseVal = if (stockInPortfolio != null) {
                            (stockInPortfolio.shares * livePrice).toLong()
                        } else 0L
                        val subsidiariesVal = s.subsidiaries.sumOf { sub ->
                            val catItem = getCatalogItem(sub.catalogId, tempState)
                            if (catItem != null) getBusinessValuation(sub, catItem) else 0L
                        }
                        baseVal + s.companyCash.toLong() + subsidiariesVal
                    } else {
                        val cat = getCatalogItem(s.catalogId, tempState)
                        if (cat != null) getBusinessValuation(s, cat) else 0L
                    }
                } 
            }
            val stocksValue = tempState.ownedStocks.sumOf { owned ->
                val livePrice = _stockList.value.find { it.ticker == owned.ticker }?.currentPrice ?: owned.averagePrice
                (owned.shares * livePrice).toLong()
            }
            val baseMegaValuation = if (tempState.megaHolding.includesInvestments) businessValue + stocksValue else businessValue
            val activeMegaValue = if (tempState.megaHolding.isActive) (baseMegaValuation * (tempState.megaHolding.ownershipPercentage / 100.0)).toLong() else businessValue

            val otherAssetsVal = tempState.ownedCrypto.sumOf { c ->
                val livePrice = _cryptoList.value.find { it.symbol == c.symbol }?.currentPrice ?: c.averagePrice
                (c.amount * livePrice).toLong()
            } + tempState.ownedProperties.sumOf { it.purchasedPrice } + tempState.ownedCollections.sumOf { it.purchasedPrice } + tempState.ownedMetals.entries.sumOf { (id, amt) ->
                val cp = _preciousMetalsList.value.find { it.id == id }?.currentPrice ?: 0.0
                (amt * cp).toLong()
            } + tempState.ownedHouses.sumOf { it.purchasedPrice } + tempState.timeDeposits.sumOf { it.principal }
            
            val newNetWorth = tempState.cash + otherAssetsVal + activeMegaValue + if (tempState.megaHolding.isActive && !tempState.megaHolding.includesInvestments) stocksValue else 0L

            // Step 4: Save Data
            _playerState.value = tempState.copy(netWorth = newNetWorth)
            saveState(_playerState.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openRestaurantBranch(instanceId: String, cost: Long, branchName: String): String? {
        val state = _playerState.value
        
        var modified = false
        var insufficientFunds = false
        
        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                if (it.companyCash < cost) {
                    insufficientFunds = true
                    it
                } else {
                    modified = true
                    val newBranch = com.example.data.OwnedBusiness(
                        instanceId = java.util.UUID.randomUUID().toString(),
                        catalogId = "RESTAURANT_BRANCH",
                        customName = branchName.ifBlank { "Cabang " + (it.subsidiaries.size + 1) },
                        level = 1,
                        isUpgrading = true,
                        upgradeDelayMonths = 3
                    )
                    it.copy(companyCash = it.companyCash - cost, subsidiaries = it.subsidiaries + newBranch)
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    if (s.companyCash < cost) {
                        insufficientFunds = true
                        s
                    } else {
                        modified = true
                        val newBranch = com.example.data.OwnedBusiness(
                            instanceId = java.util.UUID.randomUUID().toString(),
                            catalogId = "RESTAURANT_BRANCH",
                            customName = branchName.ifBlank { "Cabang " + (s.subsidiaries.size + 1) },
                            level = 1,
                            isUpgrading = true,
                            upgradeDelayMonths = 3
                        )
                        s.copy(companyCash = s.companyCash - cost, subsidiaries = s.subsidiaries + newBranch)
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }
        
        if (insufficientFunds) return "Kas Perusahaan tidak cukup."
        if (!modified) return "Bisnis tidak ditemukan."
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun upgradeRestaurantBranch(parentInstanceId: String, branchId: String, action: String, actionCost: Long): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null
        
        val cost = actionCost
        val delayTime = if (action == "LEVEL_UP") 2 else 4

        val newBusinesses = state.ownedBusinesses.map { parent ->
            if (parent.instanceId == parentInstanceId) {
                if (parent.companyCash < cost) {
                    errorMsg = "Kas Perusahaan tidak cukup."
                    parent
                } else {
                    val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                    if (branchIndex != -1) {
                        val branchLevel = parent.subsidiaries[branchIndex].level
                        val durationInSeconds = 30 + (branchLevel * 12)
                        if (!parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                            modified = true
                            val updatedBranch = parent.subsidiaries[branchIndex].copy(
                                isUpgradingRealTime = true,
                                upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L),
                                pendingAction = action
                            )
                            val newList = parent.subsidiaries.toMutableList()
                            newList[branchIndex] = updatedBranch
                            parent.copy(companyCash = parent.companyCash - cost, subsidiaries = newList)
                        } else {
                            if (errorMsg == null) errorMsg = "Sedang diproses."
                            parent
                        }
                    } else {
                        if (errorMsg == null) errorMsg = "Cabang tidak ditemukan."
                        parent
                    }
                }
            } else parent
        }
        
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { parent ->
                if (parent.instanceId == parentInstanceId) {
                    if (parent.companyCash < cost) {
                        errorMsg = "Kas Perusahaan tidak cukup."
                        parent
                    } else {
                        val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                        val branchLevel = parent.subsidiaries[branchIndex].level
                        val durationInSeconds = 30 + (branchLevel * 12)
                        if (branchIndex != -1 && !parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                            modified = true
                            val updatedBranch = parent.subsidiaries[branchIndex].copy(
                                isUpgradingRealTime = true,
                                upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L),
                                pendingAction = action
                            )
                            val newList = parent.subsidiaries.toMutableList()
                            newList[branchIndex] = updatedBranch
                            parent.copy(companyCash = parent.companyCash - cost, subsidiaries = newList)
                        } else {
                            if (branchIndex != -1 && errorMsg == null) errorMsg = "Sedang diproses." else if (errorMsg == null) errorMsg = "Cabang tidak ditemukan."
                            parent
                        }
                    }
                } else parent
            }
            h.copy(subsidiaries = newSubs)
        }
        
        if (errorMsg != null) return errorMsg
        if (!modified) return "Gagal update."
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun finishRestaurantBranchRealtimeUpgrade(parentInstanceId: String, branchId: String) {
        val state = _playerState.value
        
        val newBusinesses = state.ownedBusinesses.map { parent ->
            if (parent.instanceId == parentInstanceId) {
                val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                if (branchIndex != -1 && parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                    val branch = parent.subsidiaries[branchIndex]
                    val updatedBranch = when (branch.pendingAction) {
                        "LEVEL_UP" -> branch.copy(level = branch.level + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        "MICHELIN" -> branch.copy(michelinStars = branch.michelinStars + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        else -> branch.copy(isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                    }
                    val newList = parent.subsidiaries.toMutableList()
                    newList[branchIndex] = updatedBranch
                    parent.copy(subsidiaries = newList)
                } else parent
            } else parent
        }
        
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { parent ->
                if (parent.instanceId == parentInstanceId) {
                    val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                    if (branchIndex != -1 && parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                        val branch = parent.subsidiaries[branchIndex]
                        val updatedBranch = when (branch.pendingAction) {
                            "LEVEL_UP" -> branch.copy(level = branch.level + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                            "MICHELIN" -> branch.copy(michelinStars = branch.michelinStars + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                            else -> branch.copy(isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        }
                        val newList = parent.subsidiaries.toMutableList()
                        newList[branchIndex] = updatedBranch
                        parent.copy(subsidiaries = newList)
                    } else parent
                } else parent
            }
            h.copy(subsidiaries = newSubs)
        }
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun startConstructionTender(instanceId: String, name: String, contractValue: Long, duration: Int, initialCapital: Long, useCompanyCash: Boolean): String? {
        val state = _playerState.value
        
        var modified = false
        val newProject = com.example.data.ConstructionProject(
            name = name,
            totalContractValue = contractValue.toDouble(),
            durationMonths = duration,
            remainingMonths = duration,
            isFinished = false
        )

        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                if (useCompanyCash) {
                    if (it.companyCash < initialCapital) return "Kas Perusahaan tidak cukup."
                    modified = true
                    it.copy(companyCash = it.companyCash - initialCapital, activeTenders = it.activeTenders + newProject)
                } else {
                    if (state.cash < initialCapital) return "Dana Kas Pribadi tidak cukup."
                    modified = true
                    it.copy(activeTenders = it.activeTenders + newProject)
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    if (useCompanyCash) {
                         if (s.companyCash < initialCapital) return "Kas Perusahaan tidak cukup."
                         modified = true
                         s.copy(companyCash = s.companyCash - initialCapital, activeTenders = s.activeTenders + newProject)
                    } else {
                         if (state.cash < initialCapital) return "Dana Kas Pribadi tidak cukup."
                         modified = true
                         s.copy(activeTenders = s.activeTenders + newProject)
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (modified) {
            _playerState.value = state.copy(
                cash = if (!useCompanyCash) state.cash - initialCapital else state.cash,
                ownedBusinesses = newBusinesses,
                holdingCompanies = newHoldings
            )
            saveState(_playerState.value)
            return null
        }
        return "Bisnis tidak ditemukan."
    }

    fun takeClientProject(instanceId: String, projectId: String): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                val proj = it.availableClientProjects.find { p -> p.id == projectId }
                if (proj != null) {
                    modified = true
                    it.copy(
                        availableClientProjects = it.availableClientProjects.filterNot { p -> p.id == projectId },
                        activeTenders = it.activeTenders + proj
                    )
                } else {
                    if (errorMsg == null) errorMsg = "Proyek tidak ditemukan."
                    it
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    val proj = s.availableClientProjects.find { p -> p.id == projectId }
                    if (proj != null) {
                        modified = true
                        s.copy(
                            availableClientProjects = s.availableClientProjects.filterNot { p -> p.id == projectId },
                            activeTenders = s.activeTenders + proj
                        )
                    } else {
                        if (errorMsg == null) errorMsg = "Proyek tidak ditemukan."
                        s
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (errorMsg != null && !modified) return errorMsg
        if (!modified) return "Instansi Bisnis tidak ditemukan."

        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun buildHealthcareUnit(instanceId: String, name: String, type: String, vendorId: String?, level: Int = 1): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        val cost = when (type) {
            "HOSPITAL" -> (500_000L).toDouble() + (level * 100_000L)
            "INSURANCE" -> 2_000_000.0
            "CLINIC" -> 150_000.0
            else -> 0.0
        }

        if (state.cash < cost.toLong()) return "Dana Kas Pribadi tidak cukup."

        val isUpgrading = vendorId != null
        val delayMonths = if (isUpgrading) 3 else 0

        val newUnit = com.example.data.HealthcareUnit(
            name = name,
            type = type,
            level = level,
            isUpgrading = isUpgrading,
            upgradeDelayMonths = delayMonths,
            unitCash = if (type == "INSURANCE") 1_000_000.0 else 0.0 // Starting cash for insurance
        )

        var constructorProfit = 0L
        if (vendorId != null) {
            constructorProfit = (cost * 0.4).toLong()
        }

        val newBusinesses = state.ownedBusinesses.map { biz ->
            if (biz.instanceId == instanceId) {
                modified = true
                biz.copy(healthcareSubsidiaries = biz.healthcareSubsidiaries + newUnit)
            } else if (biz.instanceId == vendorId) {
                val newTender = com.example.data.ConstructionProject(
                    name = "Internal: $name",
                    totalContractValue = constructorProfit.toDouble(),
                    durationMonths = delayMonths,
                    remainingMonths = delayMonths
                )
                biz.copy(companyCash = biz.companyCash + constructorProfit.toDouble(), activeTenders = biz.activeTenders + newTender)
            } else biz
        }

        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    modified = true
                    s.copy(healthcareSubsidiaries = s.healthcareSubsidiaries + newUnit)
                } else if (s.instanceId == vendorId) {
                    val newTender = com.example.data.ConstructionProject(
                        name = "Internal: $name",
                        totalContractValue = constructorProfit.toDouble(),
                        durationMonths = delayMonths,
                        remainingMonths = delayMonths
                    )
                    s.copy(companyCash = s.companyCash + constructorProfit.toDouble(), activeTenders = s.activeTenders + newTender)
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (errorMsg != null) return errorMsg
        if (!modified) return "Instansi Bisnis tidak ditemukan."

        _playerState.value = state.copy(cash = state.cash - cost.toLong(), ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    private fun updateEoBusiness(instanceId: String, updateBlock: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness): Boolean {
        val state = _playerState.value
        var modified = false
        val newBusinesses = state.ownedBusinesses.map { s ->
            if (s.instanceId == instanceId) {
                modified = true
                updateBlock(s)
            } else s
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    modified = true
                    updateBlock(s)
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }
        if (modified) {
            _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
            saveState(_playerState.value)
        }
        return modified
    }

    fun getAssetPurchasePrice(asset: String): Double {
        return when (asset) {
            "Stage" -> 20000.0
            "Sound" -> 15000.0
            "Lighting" -> 12000.0
            "LED" -> 25000.0
            "Power" -> 15000.0
            "Security" -> 8000.0
            "Toilet" -> 5000.0
            "Barricade" -> 6000.0
            "Ambulance" -> 30000.0
            "Tent" -> 10000.0
            "Truss" -> 15000.0
            "Forklift" -> 12000.0
            "Truck" -> 18000.0
            "Warehouse" -> 50000.0
            "Helicopter" -> 500000.0
            else -> 10000.0
        }
    }

    fun getDivisionHiringCost(div: String): Double {
        return when (div) {
            "Sales" -> 15000.0
            "Creative" -> 12000.0
            "Production" -> 20000.0
            "Multimedia" -> 18000.0
            "Talent" -> 25000.0
            "Logistics" -> 15000.0
            "Finance" -> 22000.0
            "Legal" -> 25000.0
            "Marketing" -> 15000.0
            else -> 10000.0
        }
    }

    fun getHqUpgradeCost(hq: String): Double {
        return when (hq) {
            "HOUSE" -> 0.0
            "OFFICE" -> 50000.0
            "REGIONAL" -> 200000.0
            "NATIONAL" -> 1000000.0
            "INTERNATIONAL" -> 5000000.0
            else -> 0.0
        }
    }

    fun formRentalDivision(instanceId: String): String? {
        val state = _playerState.value
        val cost = 100000L
        if (state.cash < cost) return "Kas pribadi kurang dari $100,000"
        
        var success = false
        val ok = updateEoBusiness(instanceId) { biz ->
            success = true
            biz.copy(hasRentalDivision = true)
        }
        if (ok && success) {
            _playerState.value = _playerState.value.copy(cash = _playerState.value.cash - cost)
            saveState(_playerState.value)
            return null
        }
        return "Bisnis tidak ditemukan."
    }

    fun acceptClientEvent(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.clientEventRequests.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Tawaran tidak valid."
                biz
            } else {
                val newEv = ev.copy(phase = "PLANNING")
                biz.copy(
                    clientEventRequests = biz.clientEventRequests.filter { it.id != eventId },
                    activeEvents = biz.activeEvents + newEv
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startCustomEvent(instanceId: String, name: String, category: String, pax: Int, tb: Double, eoFee: Double, techFee: Double, useInHouse: Boolean): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val requirements = when (category) {
                "Birthday Party" -> listOf("Sound", "Lighting")
                "Wedding" -> listOf("Stage", "Sound", "Lighting", "LED")
                "Graduation" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "Corporate Gathering" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "Concert" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance")
                "Festival" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Tent")
                "Exhibition" -> listOf("LED", "Power", "Security", "Toilet", "Barricade", "Forklift", "Truck")
                "Sports Event" -> listOf("Sound", "Power", "Security", "Toilet", "Barricade", "Ambulance")
                "Government Event" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "International Summit" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter")
                else -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
            }
            val weather = if (kotlin.random.Random.nextDouble() < 0.3) "RAINY" else "SUNNY"
            val isOutdoor = listOf("Wedding", "Pensi", "Konser", "Festival", "Sports").any { category.contains(it, ignoreCase = true) }
            val event = com.example.data.EventProject(
                name = name,
                category = category,
                pax = pax,
                totalBudget = tb,
                eoFee = eoFee,
                techFee = techFee,
                useInHouseTech = useInHouse,
                executionEndTime = 0L,
                weather = weather,
                isOutdoor = isOutdoor,
                requirements = requirements,
                phase = "PLANNING"
            )
            biz.copy(activeEvents = biz.activeEvents + event)
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startSpecialEvent(instanceId: String, specName: String, specCategory: String, budget: Double, fee: Double, requirements: List<String>, prestigeReward: Int): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            if (biz.eoCompletedSpecialEvents.contains(specName)) {
                errorMsg = "Kamu sudah menyelesaikan event prestige ini!"
                biz
            } else if (biz.eoPrestige < prestigeReward - 25) { // some threshold
                errorMsg = "Prestige perusahaan kamu belum cukup untuk event akbar ini!"
                biz
            } else {
                val weather = if (kotlin.random.Random.nextDouble() < 0.3) "RAINY" else "SUNNY"
                val event = com.example.data.EventProject(
                    name = specName,
                    category = specCategory,
                    pax = 10000,
                    totalBudget = budget,
                    eoFee = fee,
                    techFee = budget * 0.4,
                    useInHouseTech = false,
                    executionEndTime = 0L,
                    weather = weather,
                    isOutdoor = true,
                    requirements = requirements,
                    phase = "PLANNING",
                    isSpecial = true,
                    requiredPrestige = prestigeReward // Store the reward here for ease of access
                )
                biz.copy(activeEvents = biz.activeEvents + event)
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun rentAssetForEvent(instanceId: String, eventId: String, reqName: String): String? {
        var errorMsg: String? = null
        val rentCost = getAssetPurchasePrice(reqName) * 0.05
        
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else if (biz.companyCash < rentCost) {
                errorMsg = "Kas internal tidak mencukupi untuk sewa vendor ($${String.format("%,.0f", rentCost)})."
                biz
            } else {
                val updatedEv = ev.copy(rentedAssets = ev.rentedAssets + reqName)
                biz.copy(
                    companyCash = biz.companyCash - rentCost,
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startEventExecution(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else if (biz.companyCash < ev.totalBudget) {
                errorMsg = "Kas internal tidak cukup modal untuk mengeksekusi event ($${String.format("%,.0f", ev.totalBudget)})."
                biz
            } else {
                // Random incident: 40% chance
                val hasIncident = kotlin.random.Random.nextDouble() < 0.40
                val incidentName = if (hasIncident) {
                    listOf("GENSET_BROKEN", "VENDOR_LATE", "ARTIST_CANCELED", "TICKETS_OVERSOLD", "DEMONSTRATION", "STRONG_WINDS").random()
                } else null
                
                // Duration of event execution in milliseconds: 30 seconds
                val durationMs = 30000L
                val updatedEv = ev.copy(
                    phase = "EXECUTING",
                    executionStartTime = System.currentTimeMillis(),
                    executionEndTime = System.currentTimeMillis() + durationMs,
                    activeIncident = incidentName,
                    incidentResolved = false,
                    incidentImpactQuality = 0.0,
                    incidentImpactCost = 0.0
                )
                biz.copy(
                    companyCash = biz.companyCash - ev.totalBudget,
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun resolveEventIncident(instanceId: String, eventId: String, choiceIndex: Int): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null || ev.activeIncident == null) {
                errorMsg = "Incident tidak aktif."
                biz
            } else {
                var qualityImpact = 0.0
                var costImpact = 0.0
                
                when (ev.activeIncident) {
                    "GENSET_BROKEN" -> {
                        if (choiceIndex == 0) { // Emergency generator
                            costImpact = 10000.0
                            qualityImpact = 0.0
                        } else if (choiceIndex == 1) { // Backup generator (if owned)
                            if ((biz.eoOwnedAssets["Power"] ?: 0) > 0) {
                                costImpact = 0.0
                                qualityImpact = 0.0
                            } else {
                                costImpact = 5000.0
                                qualityImpact = -10.0
                            }
                        } else { // Do nothing
                            costImpact = 0.0
                            qualityImpact = -30.0
                        }
                    }
                    "VENDOR_LATE" -> {
                        if (choiceIndex == 0) { // Pay courier
                            costImpact = 5000.0
                            qualityImpact = 0.0
                        } else { // Wait
                            costImpact = 0.0
                            qualityImpact = -15.0
                        }
                    }
                    "ARTIST_CANCELED" -> {
                        if (choiceIndex == 0) { // Replace artist
                            costImpact = 20000.0
                            qualityImpact = 0.0
                        } else if (choiceIndex == 1) { // Negotiate discount
                            costImpact = 8000.0
                            qualityImpact = -10.0
                        } else { // Cancel parts
                            costImpact = 0.0
                            qualityImpact = -40.0
                        }
                    }
                    "TICKETS_OVERSOLD" -> {
                        if (choiceIndex == 0) { // Upgrade area
                            costImpact = 15000.0
                            qualityImpact = 0.0
                        } else { // Leave crowded
                            costImpact = 0.0
                            qualityImpact = -25.0
                        }
                    }
                    "DEMONSTRATION" -> {
                        if (choiceIndex == 0) { // Extra security
                            costImpact = 12000.0
                            qualityImpact = 0.0
                        } else { // Negotiate
                            costImpact = 0.0
                            qualityImpact = -20.0
                        }
                    }
                    "STRONG_WINDS" -> {
                        if (choiceIndex == 0) { // Reinforce rigging
                            costImpact = 8000.0
                            qualityImpact = 0.0
                        } else { // Do nothing
                            costImpact = 0.0
                            qualityImpact = -30.0
                        }
                    }
                }
                
                if (biz.companyCash < costImpact) {
                    errorMsg = "Kas internal tidak cukup untuk keputusan ini!"
                    biz
                } else {
                    val updatedEv = ev.copy(
                        incidentResolved = true,
                        incidentImpactQuality = qualityImpact,
                        incidentImpactCost = costImpact
                    )
                    biz.copy(
                        companyCash = biz.companyCash - costImpact,
                        activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                    )
                }
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun calculateEventResults(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else {
                val divisions = biz.eoDivisions ?: emptySet()
                
                // 1. Calculate Quality
                var baseQuality = 100.0
                if (divisions.contains("Creative")) {
                    baseQuality += 5.0
                }
                
                // Missing assets deductions
                var missingCount = 0
                for (req in ev.requirements) {
                    val isOwned = (biz.eoOwnedAssets[req] ?: 0) > 0
                    val isRented = ev.rentedAssets.contains(req)
                    if (!isOwned && !isRented) {
                        missingCount++
                    }
                }
                val requirementDeduction = missingCount * 12.0
                
                // Weather impact
                var weatherDeduction = 0.0
                var weatherCost = 0.0
                if (ev.isOutdoor && ev.weather == "RAINY") {
                    val hasTent = (biz.eoOwnedAssets["Tent"] ?: 0) > 0 || ev.rentedAssets.contains("Tent")
                    val hasTruss = (biz.eoOwnedAssets["Truss"] ?: 0) > 0 || ev.rentedAssets.contains("Truss")
                    if (hasTent || hasTruss) {
                        weatherCost = 1000.0
                    } else {
                        weatherDeduction = 25.0
                        weatherCost = 5000.0
                    }
                }
                
                // Incident impact
                var incidentDeduction = 0.0
                if (ev.activeIncident != null) {
                    if (!ev.incidentResolved) {
                        incidentDeduction = when (ev.activeIncident) {
                            "GENSET_BROKEN" -> 30.0
                            "VENDOR_LATE" -> 15.0
                            "ARTIST_CANCELED" -> 40.0
                            "TICKETS_OVERSOLD" -> 25.0
                            "DEMONSTRATION" -> 20.0
                            "STRONG_WINDS" -> 30.0
                            else -> 20.0
                        }
                    } else {
                        incidentDeduction = ev.incidentImpactQuality
                    }
                }
                
                val finalQuality = (baseQuality - requirementDeduction - weatherDeduction - incidentDeduction).coerceIn(0.0, 100.0)
                
                // 2. Star rating
                var rating = when {
                    finalQuality >= 90.0 -> 5.0
                    finalQuality >= 75.0 -> 4.0
                    finalQuality >= 55.0 -> 3.0
                    finalQuality >= 35.0 -> 2.0
                    else -> 1.0
                }
                if (divisions.contains("Talent")) {
                    rating = (rating + 0.3).coerceAtMost(5.0)
                }
                
                val reviewText = when {
                    rating >= 4.8 -> "SANGAT LUAR BIASA! Semua terencana dengan sempurna, profesional, dan klien sangat puas dengan hasil kerja kami!"
                    rating >= 4.0 -> "Acara berjalan dengan lancar dan cukup rapi. Klien puas dan memberikan feedback positif."
                    rating >= 3.0 -> "Biasa saja. Beberapa kendala teknis kecil mengganggu jalannya acara, tapi overall ok."
                    rating >= 2.0 -> "Cukup mengecewakan. Banyak fasilitas utama yang kurang lengkap dan tidak sesuai ekspektasi."
                    else -> "Bencana total! Acara hancur lebur, genset bermasalah, dan penonton ricuh! Media massa menyoroti kegagalan fatal kami."
                }
                
                // 3. Profit calculations
                var profit = ev.eoFee
                var multiplier = 1.0
                if (divisions.contains("Production")) multiplier += 0.10
                if (divisions.contains("Finance")) multiplier += 0.05
                profit *= multiplier
                profit -= weatherCost
                
                val updatedEv = ev.copy(
                    phase = "REVIEW",
                    quality = finalQuality,
                    resultRating = rating,
                    resultReviewText = reviewText,
                    finalProfit = profit
                )
                
                biz.copy(
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun collectEventEarnings(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null || ev.phase != "REVIEW") {
                errorMsg = "Event belum selesai direview."
                biz
            } else {
                val rating = ev.resultRating
                val divisions = biz.eoDivisions ?: emptySet()
                
                // Reputation change
                var repChange = when {
                    rating >= 4.5 -> 8.0
                    rating >= 3.5 -> 4.0
                    rating >= 2.5 -> 1.0
                    rating >= 1.5 -> -5.0
                    else -> -15.0
                }
                if (repChange > 0 && divisions.contains("Marketing")) {
                    repChange *= 1.20
                }
                if (repChange < 0 && divisions.contains("Legal")) {
                    repChange *= 0.50
                }
                
                val newReputation = (biz.eoReputation + repChange).coerceIn(0.0, 100.0)
                
                // Prestige points
                val basePrestigeReward = when (ev.category) {
                    "Birthday Party" -> 1
                    "Wedding" -> 2
                    "Graduation" -> 2
                    "Corporate Gathering" -> 3
                    "Concert" -> 5
                    "Festival" -> 6
                    "Exhibition" -> 4
                    "Sports Event" -> 5
                    "Government Event" -> 6
                    "International Summit" -> 10
                    else -> 1
                }
                val totalPrestigeReward = if (ev.isSpecial) ev.requiredPrestige else basePrestigeReward
                
                val newPrestige = biz.eoPrestige + totalPrestigeReward
                val newSpecialEvents = if (ev.isSpecial) biz.eoCompletedSpecialEvents + ev.name else biz.eoCompletedSpecialEvents
                val totalPayout = ev.totalBudget + ev.finalProfit
                
                biz.copy(
                    companyCash = biz.companyCash + totalPayout,
                    eoReputation = newReputation,
                    eoPrestige = newPrestige,
                    eoCompletedSpecialEvents = newSpecialEvents,
                    activeEvents = biz.activeEvents.filter { it.id != eventId }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun upgradeEoHq(instanceId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val currentHq = biz.eoCompanyHqLevel ?: "HOUSE"
            val nextHq = when (currentHq) {
                "HOUSE" -> "OFFICE"
                "OFFICE" -> "REGIONAL"
                "REGIONAL" -> "NATIONAL"
                "NATIONAL" -> "INTERNATIONAL"
                else -> null
            }
            if (nextHq == null) {
                errorMsg = "HQ sudah berada di tingkat maksimal!"
                biz
            } else {
                val cost = getHqUpgradeCost(nextHq)
                if (biz.companyCash < cost) {
                    errorMsg = "Kas internal tidak cukup untuk upgrade HQ ($${String.format("%,.0f", cost)})."
                    biz
                } else {
                    biz.copy(
                        eoCompanyHqLevel = nextHq,
                        companyCash = biz.companyCash - cost
                    )
                }
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun hireEoDivision(instanceId: String, divName: String): String? {
        var errorMsg: String? = null
        val cost = getDivisionHiringCost(divName)
        val ok = updateEoBusiness(instanceId) { biz ->
            val divisions = biz.eoDivisions ?: emptySet()
            if (divisions.contains(divName)) {
                errorMsg = "Divisi ini sudah dibentuk."
                biz
            } else if (biz.companyCash < cost) {
                errorMsg = "Kas internal tidak cukup untuk membentuk divisi $divName ($${String.format("%,.0f", cost)})."
                biz
            } else {
                biz.copy(
                    eoDivisions = divisions + divName,
                    companyCash = biz.companyCash - cost
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun buyEoAsset(instanceId: String, assetName: String): String? {
        var errorMsg: String? = null
        val cost = getAssetPurchasePrice(assetName)
        val ok = updateEoBusiness(instanceId) { biz ->
            val owned = biz.eoOwnedAssets ?: emptyMap()
            if (biz.companyCash < cost) {
                errorMsg = "Kas internal tidak cukup untuk membeli aset $assetName ($${String.format("%,.0f", cost)})."
                biz
            } else {
                val currentCount = owned[assetName] ?: 0
                val updatedOwned = owned.toMutableMap()
                updatedOwned[assetName] = currentCount + 1
                biz.copy(
                    eoOwnedAssets = updatedOwned,
                    companyCash = biz.companyCash - cost
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    data class EventTemplate(
        val category: String,
        val minPax: Int,
        val maxPax: Int,
        val baseCostPerPax: Double,
        val feePercent: Double,
        val isOutdoor: Boolean,
        val requirements: List<String>,
        val requiredHqs: List<String>,
        val minReputation: Double,
        val tier: Int
    )

    private val eventTemplates = listOf(
        EventTemplate("Birthday Party", 50, 200, 30.0, 0.30, false, listOf("Sound", "Lighting"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 0.0, 2),
        EventTemplate("Wedding", 200, 1000, 50.0, 0.35, true, listOf("Stage", "Sound", "Lighting", "LED"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 10.0, 3),
        EventTemplate("Graduation", 300, 1500, 20.0, 0.20, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 20.0, 3),
        EventTemplate("Corporate Gathering", 100, 500, 80.0, 0.25, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 30.0, 4),
        EventTemplate("Concert", 3000, 20000, 40.0, 0.30, true, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 50.0, 4),
        EventTemplate("Festival", 5000, 40000, 50.0, 0.40, true, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Tent"), listOf("NATIONAL", "INTERNATIONAL"), 65.0, 4),
        EventTemplate("Exhibition", 1000, 10000, 35.0, 0.25, false, listOf("LED", "Power", "Security", "Toilet", "Barricade", "Forklift", "Truck"), listOf("OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 40.0, 4),
        EventTemplate("Sports Event", 5000, 30000, 25.0, 0.20, true, listOf("Sound", "Power", "Security", "Toilet", "Barricade", "Ambulance"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 55.0, 4),
        EventTemplate("Government Event", 500, 3000, 60.0, 0.35, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 70.0, 4),
        EventTemplate("International Summit", 1000, 5000, 150.0, 0.30, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter"), listOf("NATIONAL", "INTERNATIONAL"), 85.0, 5)
    )

    fun generateEventRequestsForBusiness(owned: com.example.data.OwnedBusiness): List<com.example.data.EventProject> {
        val hq = owned.eoCompanyHqLevel ?: "HOUSE"
        val reputation = owned.eoReputation
        val divisions = owned.eoDivisions ?: emptySet()
        
        val filtered = eventTemplates.filter { t ->
            t.requiredHqs.contains(hq) && reputation >= t.minReputation
        }
        val pool = if (filtered.isEmpty()) {
            eventTemplates.filter { it.category == "Birthday Party" }
        } else {
            filtered
        }
        
        var count = (2..5).random()
        if (divisions.contains("Marketing")) {
            count += (1..2).random()
        }
        
        val list = mutableListOf<com.example.data.EventProject>()
        for (i in 0 until count) {
            val template = pool.random()
            val pax = (template.minPax..template.maxPax).random()
            var tb = pax * template.baseCostPerPax * (0.8 + kotlin.random.Random.nextDouble(0.0, 0.4))
            
            if (divisions.contains("Sales")) {
                tb *= 1.15
            }
            
            val eoFee = tb * template.feePercent
            val techFee = tb * 0.40
            val weather = if (kotlin.random.Random.nextDouble() < 0.30) "RAINY" else "SUNNY"
            
            val clientPrefix = when (template.tier) {
                2 -> listOf("Bpk. Budi", "Siska & Rio", "Ibu Dewi", "SMA 1 Merdeka").random()
                3 -> listOf("CV Jaya Makmur", "Universitas Abadi", "Pemkot Regional", "Konser Indie Bandung").random()
                4 -> listOf("PT Telkom Indonesia", "Pesta Rakyat Raya", "Pemerintah Nasional", "Festival Musik Kebangsaan").random()
                else -> listOf("G20 Secretariat", "World Expo Group", "Asian Games Council", "International Summit Org").random()
            }
            
            list.add(com.example.data.EventProject(
                name = "$clientPrefix - ${template.category}",
                category = template.category,
                pax = pax,
                totalBudget = tb,
                eoFee = eoFee,
                techFee = techFee,
                useInHouseTech = false,
                executionEndTime = 0L,
                tier = template.tier,
                isSpecial = false,
                weather = weather,
                isOutdoor = template.isOutdoor,
                requirements = template.requirements,
                phase = "PLANNING"
            ))
        }
        return list
    }

    fun completeEventProject(instanceId: String, eventId: String) {
        // Keeps backwards compatibility of the method name just in case
        calculateEventResults(instanceId, eventId)
    }

    // ==========================================
    // FAMILY OFFICE ACTIONS
    // ==========================================

    fun updateMonthlyCeoSalary(salary: Long) {
        val state = _playerState.value
        _playerState.value = state.copy(monthlyCeoSalary = salary)
        saveState(_playerState.value)
    }

    fun submitCeoSalaryRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingCeoSalaryPercent = proposedPercent,
            boardApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun dismissBoardReplyMessage() {
        val state = _playerState.value
        _playerState.value = state.copy(boardReplyMessage = null)
        saveState(_playerState.value)
    }

    fun submitDividendRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingDividendPercent = proposedPercent,
            dividendApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun submitTantiemRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingTantiemPercent = proposedPercent,
            tantiemApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun withdrawCorporateDividends(percent: Double): String? {
        val state = _playerState.value
        val totalHoldingCash = state.holdingCompanies.sumOf { it.holdingCash } + state.ownedBusinesses.sumOf { it.companyCash }
        if (totalHoldingCash <= 0.0) return "Tidak ada kas kas holding/perusahaan untuk ditarik."
        
        val amountRequested = totalHoldingCash * (percent / 100.0)
        if (amountRequested <= 0) return "Jumlah penarikan harus lebih besar dari 0"
        
        var remainingDeduct = amountRequested
        val updatedHoldings = state.holdingCompanies.map { h ->
            if (remainingDeduct <= 0.0) h
            else {
                val v = h.holdingCash
                val d = Math.min(v, remainingDeduct)
                remainingDeduct -= d
                h.copy(holdingCash = v - d)
            }
        }
        val updatedBusinesses = state.ownedBusinesses.map { b ->
            if (remainingDeduct <= 0.0) b
            else {
                val v = b.companyCash
                val d = Math.min(v, remainingDeduct)
                remainingDeduct -= d
                b.copy(companyCash = v - d)
            }
        }
        
        val netToPlayer = (amountRequested * 0.85).toLong()
        val taxPaid = (amountRequested * 0.15).toLong()
        
        val newState = state.copy(
            holdingCompanies = updatedHoldings,
            ownedBusinesses = updatedBusinesses,
            cash = state.cash + netToPlayer
        )
        
        val newsList = listOf(
            MarketNews(
                id = "fo_dividend_${System.currentTimeMillis()}",
                text = "FAMILY OFFICE: Berhasil mencairkan dividen perusahaan sebesar $${com.example.ui.formatCurrencyRingkas(amountRequested, false)}. Dikenakan pajak dividen 15% ($${com.example.ui.formatCurrencyRingkas(taxPaid.toDouble(), false)}). Bersih diterima: $${com.example.ui.formatCurrencyRingkas(netToPlayer.toDouble(), false)}.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun borrowLombardLoan(amount: Long, limitLTV: Double = 0.20): String? {
        val state = _playerState.value
        
        val totalBusinessValuation = state.ownedBusinesses.sumOf {
            val catalogItem = com.example.data.getCatalogItem(it.catalogId, state)
            if (catalogItem != null) com.example.data.getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = state.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = com.example.data.getCatalogItem(sub.catalogId, state)
                if (catalogItem != null) com.example.data.getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        val businessValuation = totalBusinessValuation + totalHoldingValuation
        val maxAllowedDebt = (businessValuation * limitLTV).toLong()
        
        if (state.personalDebt + amount > maxAllowedDebt) {
            return "Limit utang terlampaui! Maksimal LTV adalah 20% dari Valuasi Bisnis ($${com.example.ui.formatCurrencyRingkas(maxAllowedDebt.toDouble(), false)})."
        }
        
        val nextState = state.copy(
            personalDebt = state.personalDebt + amount,
            privateBalance = state.privateBalance + amount
        )
        val newState = logToPrivateLedger(nextState, "Pencairan Pinjaman Lombard (Agunan Saham)", amount, true)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_lombard_borrow_${System.currentTimeMillis()}",
                text = "LOMBARD LOAN: Anda berhasil mencairkan pinjaman agunan saham sebesar $${com.example.ui.formatCurrencyRingkas(amount.toDouble(), false)}. Kas Pribadi bertambah, utang bertambah.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun repayLombardLoan(amount: Long): String? {
        val state = _playerState.value
        if (amount <= 0) return "Jumlah pembayaran harus lebih dari 0"
        if (state.privateBalance < amount) return "Kas pribadi Anda tidak mencukupi untuk pembayaran ini."
        
        val repayAmount = Math.min(amount, state.personalDebt)
        
        val nextState = state.copy(
            personalDebt = state.personalDebt - repayAmount,
            privateBalance = state.privateBalance - repayAmount
        )
        val newState = logToPrivateLedger(nextState, "Pelunasan Pinjaman Lombard", repayAmount, false)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_lombard_repay_${System.currentTimeMillis()}",
                text = "LOMBARD LOAN: Anda membayar utang agunan saham sebesar $${com.example.ui.formatCurrencyRingkas(repayAmount.toDouble(), false)} menggunakan kas pribadi.",
                type = "NEUTRAL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun sellMegaHoldingShares(percent: Double): String? {
        val state = _playerState.value
        if (percent <= 0.0 || percent > state.companyOwnershipPercent) {
            return "Persentase penjualan tidak valid atau melampaui kepemilikan Anda saat ini."
        }
        
        val totalBusinessValuation = state.ownedBusinesses.sumOf {
            val catalogItem = com.example.data.getCatalogItem(it.catalogId, state)
            if (catalogItem != null) com.example.data.getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = state.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = com.example.data.getCatalogItem(sub.catalogId, state)
                if (catalogItem != null) com.example.data.getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        val businessValuation = totalBusinessValuation + totalHoldingValuation
        
        val transactionValue = (businessValuation * (percent / 100.0)).toLong()
        val newOwnership = (state.companyOwnershipPercent - percent).coerceAtLeast(0.0)
        
        val newState = state.copy(
            companyOwnershipPercent = newOwnership,
            cash = state.cash + transactionValue,
            megaHolding = state.megaHolding.copy(ownershipPercentage = newOwnership)
        )
        
        val newsList = listOf(
            MarketNews(
                id = "fo_shares_sale_${System.currentTimeMillis()}",
                text = "SECONDARY SALE: Menjual ${String.format(java.util.Locale.US, "%.1f", percent)}% saham kepemilikan holding seharga $${com.example.ui.formatCurrencyRingkas(transactionValue.toDouble(), false)} kepada investor privat.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun calculateProgressiveTax(income: Long, serviceLevel: Int = 0): Long {
        if (income <= 0) return 0L
        var tax = 0.0
        var remaining = income.toDouble()
        
        // Bracket 1: 0 - 50,000 (5%)
        val b1 = Math.min(remaining, 50000.0)
        tax += b1 * 0.05
        remaining -= b1
        
        // Bracket 2: 50,001 - 250,000 (15%) -> size 200,000
        if (remaining > 0) {
            val b2 = Math.min(remaining, 200000.0)
            tax += b2 * 0.15
            remaining -= b2
        }
        
        // Bracket 3: 250,001 - 500,000 (25%) -> size 250,000
        if (remaining > 0) {
            val b3 = Math.min(remaining, 250000.0)
            tax += b3 * 0.25
            remaining -= b3
        }
        
        // Bracket 4: 500,001 - 5,000,000 (30% -> 25% if Tax Lawyer)
        if (remaining > 0) {
            val b4 = Math.min(remaining, 4500000.0)
            val r4 = if (serviceLevel == 2) 0.25 else 0.30
            tax += b4 * r4
            remaining -= b4
        }
        
        // Bracket 5: > 5,000,000 (35% -> 25% if Tax Lawyer)
        if (remaining > 0) {
            val r5 = if (serviceLevel == 2) 0.25 else 0.35
            tax += remaining * r5
        }
        
        return tax.toLong()
    }

    fun setPrivateTaxServiceLevel(level: Int) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(privateTaxServiceLevel = level)
        saveState(_playerState.value)
    }

    fun reportSptTahunan(): String {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            isSptReportedThisYear = true,
            consecutiveUnreportedSpt = 0
        )
        saveState(_playerState.value)
        return "SPT Tahunan & Audit Aset berhasil dilaporkan untuk tahun ini!"
    }

    // ==========================================
    // LIFESTYLE & PERSONAL SPENDING ACTIONS
    // ==========================================

    fun toggleSubscription(name: String, monthlyCost: Long) {
        val state = _playerState.value
        val isCurrentlyActive = state.activeSubscriptions.contains(name)
        val nextSubscriptions = if (isCurrentlyActive) {
            state.activeSubscriptions.filter { it != name }
        } else {
            state.activeSubscriptions + name
        }
        val nextCost = if (isCurrentlyActive) {
            (state.monthlyLifestyleCost - monthlyCost).coerceAtLeast(0L)
        } else {
            state.monthlyLifestyleCost + monthlyCost
        }
        val nextAllSubs = state.allSubscriptions.map { sub ->
            if (sub.name == name) {
                sub.copy(isActive = !isCurrentlyActive)
            } else {
                sub
            }
        }
        _playerState.value = state.copy(
            activeSubscriptions = nextSubscriptions,
            allSubscriptions = nextAllSubs,
            monthlyLifestyleCost = nextCost
        )
        saveState(_playerState.value)
    }

    fun addLifestyleItem(tabCategory: String, sectionName: String, name: String, price: Long, imgUrl: String, desc: String) {
        val state = _playerState.value
        val newItem = com.example.data.LifestyleItem(
            id = java.util.UUID.randomUUID().toString(),
            tabCategory = tabCategory,
            sectionName = sectionName,
            name = name,
            price = price,
            imgUrl = imgUrl,
            desc = desc,
            isActive = false,
            isOwned = false,
            isCustom = true
        )
        val nextAllItems = state.allSubscriptions + newItem
        _playerState.value = state.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun updateLifestyleItem(id: String, name: String, price: Long, sectionName: String, desc: String, imgUrl: String) {
        val state = _playerState.value
        val nextAllItems = state.allSubscriptions.map { item ->
            if (item.id == id) {
                val priceDiff = price - item.price
                if (item.isActive && (item.tabCategory == "langganan" || item.tabCategory == "wellness")) {
                    val nextCost = (state.monthlyLifestyleCost + priceDiff).coerceAtLeast(0L)
                    _playerState.value = _playerState.value.copy(monthlyLifestyleCost = nextCost)
                }
                item.copy(name = name, price = price, sectionName = sectionName, desc = desc, imgUrl = imgUrl)
            } else {
                item
            }
        }
        _playerState.value = _playerState.value.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun deleteLifestyleItem(id: String) {
        val state = _playerState.value
        val itemToDelete = state.allSubscriptions.find { it.id == id } ?: return
        
        val nextCost = if (itemToDelete.isActive && (itemToDelete.tabCategory == "langganan" || itemToDelete.tabCategory == "wellness")) {
            (state.monthlyLifestyleCost - itemToDelete.price).coerceAtLeast(0L)
        } else {
            state.monthlyLifestyleCost
        }
        
        val nextAllItems = state.allSubscriptions.filter { it.id != id }
        _playerState.value = state.copy(
            allSubscriptions = nextAllItems,
            monthlyLifestyleCost = nextCost
        )
        saveState(_playerState.value)
    }

    fun toggleLifestyleItemActive(id: String) {
        val state = _playerState.value
        val nextAllItems = state.allSubscriptions.map { item ->
            if (item.id == id) {
                val nextActive = !item.isActive
                val costDiff = if (nextActive) item.price else -item.price
                val nextCost = (state.monthlyLifestyleCost + costDiff).coerceAtLeast(0L)
                
                val nextActiveSubs = if (nextActive) {
                    state.activeSubscriptions + item.name
                } else {
                    state.activeSubscriptions.filter { it != item.name }
                }
                
                _playerState.value = _playerState.value.copy(
                    activeSubscriptions = nextActiveSubs,
                    monthlyLifestyleCost = nextCost
                )
                item.copy(isActive = nextActive)
            } else {
                item
            }
        }
        _playerState.value = _playerState.value.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun purchaseLifestyleItemOwned(id: String): Boolean {
        val state = _playerState.value
        val item = state.allSubscriptions.find { it.id == id } ?: return false
        if (state.privateBalance < item.price) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - item.price,
            ownedGadgets = if (item.tabCategory == "gadget") state.ownedGadgets + item.name else state.ownedGadgets,
            totalCharityDonated = if (item.tabCategory == "filantropi") state.totalCharityDonated + item.price else state.totalCharityDonated
        )
        
        val nextAllItems = reducedState.allSubscriptions.map { itm ->
            if (itm.id == id) {
                itm.copy(isOwned = true)
            } else {
                itm
            }
        }
        
        val finalState = reducedState.copy(allSubscriptions = nextAllItems)
        val ledgerTitle = when (item.tabCategory) {
            "gadget" -> "Beli Gadget: ${item.name}"
            "filantropi" -> "Donasi Filantropi: ${item.name}"
            else -> "Beli Item: ${item.name}"
        }
        _playerState.value = logToPrivateLedger(finalState, ledgerTitle, item.price, false)
        saveState(_playerState.value)
        return true
    }

    fun goOnLifestyleExpedition(id: String): Boolean {
        val state = _playerState.value
        val item = state.allSubscriptions.find { it.id == id } ?: return false
        if (state.privateBalance < item.price) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - item.price,
            travelHistory = state.travelHistory + 1
        )
        
        val ledgerTitle = "Pergi Liburan: ${item.name}"
        _playerState.value = logToPrivateLedger(reducedState, ledgerTitle, item.price, false)
        saveState(_playerState.value)
        return true
    }

    fun bookPrivateTravel(destinationId: String, days: Int, totalCost: Long, extraDetails: String): Boolean {
        val state = _playerState.value
        val destination = state.travelDestinations.find { it.id == destinationId } ?: return false
        if (state.privateBalance < totalCost) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - totalCost,
            travelHistory = state.travelHistory + 1,
            totalTripsTaken = state.totalTripsTaken + 1
        )
        
        val ledgerTitle = "Travel Concierge: ${destination.name} ($days Hari - $extraDetails)"
        _playerState.value = logToPrivateLedger(reducedState, ledgerTitle, totalCost, false)
        saveState(_playerState.value)
        return true
    }

    fun addCustomTravelDestination(name: String, region: String, pricePerDay: Long, imageUrl: String) {
        val state = _playerState.value
        val newDestination = com.example.data.TravelDestination(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            region = region,
            pricePerDay = pricePerDay,
            imageUrl = imageUrl,
            isCustom = true
        )
        val nextDestinations = state.travelDestinations + newDestination
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun deleteTravelDestination(destinationId: String) {
        val state = _playerState.value
        val nextDestinations = state.travelDestinations.filter { it.id != destinationId }
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun editTravelDestinationImageUrl(destinationId: String, newImageUrl: String) {
        val state = _playerState.value
        val nextDestinations = state.travelDestinations.map {
            if (it.id == destinationId) {
                it.copy(imageUrl = newImageUrl)
            } else {
                it
            }
        }
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun purchaseTechGadget(name: String, price: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < price) return false
        
        val nextGadgets = state.ownedGadgets + name
        val reducedState = state.copy(
            privateBalance = state.privateBalance - price,
            ownedGadgets = nextGadgets
        )
        val nextAllSubs = reducedState.allSubscriptions.map { sub ->
            if (sub.name == name) {
                sub.copy(isOwned = true)
            } else {
                sub
            }
        }
        _playerState.value = logToPrivateLedger(reducedState.copy(allSubscriptions = nextAllSubs), "Beli Gadget: $name", price, false)
        saveState(_playerState.value)
        return true
    }

    fun goOnTravelExpedition(name: String, cost: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < cost) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - cost,
            travelHistory = state.travelHistory + 1
        )
        _playerState.value = logToPrivateLedger(reducedState, "Pergi Liburan: $name", cost, false)
        saveState(_playerState.value)
        return true
    }

    fun donateToCharity(amount: Long): Boolean {
        val state = _playerState.value
        if (amount <= 0 || state.privateBalance < amount) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - amount,
            totalCharityDonated = state.totalCharityDonated + amount
        )
        _playerState.value = logToPrivateLedger(reducedState, "Donasi Amal Kemanusiaan", amount, false)
        saveState(_playerState.value)
        return true
    }

    fun createPrivateFoundation(name: String, type: com.example.data.FoundationType): Boolean {
        val state = _playerState.value
        if (state.privateBalance < type.legalCost) return false

        val newFoundation = com.example.data.FoundationEntity(
            name = name,
            type = type,
            constructionMonthsLeft = type.setupMonths,
            isLegalized = false,
            educationInstitutions = emptyList()
        )
        val nextFoundations = state.foundations + newFoundation
        val reducedState = state.copy(
            privateBalance = state.privateBalance - type.legalCost,
            foundations = nextFoundations
        )
        _playerState.value = logToPrivateLedger(reducedState, "Mendirikan Yayasan: $name (${type.label})", type.legalCost, false)
        saveState(_playerState.value)
        return true
    }

    fun deletePrivateFoundation(foundationId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val nextFoundations = state.foundations.filter { it.id != foundationId }
        val updatedState = state.copy(
            foundations = nextFoundations
        )
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Yayasan: ${foundation.name}", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun injectEndowmentFund(foundationId: String, amount: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < amount || amount <= 0) return false

        val nextFoundations = state.foundations.map {
            if (it.id == foundationId) {
                it.copy(endowmentFund = it.endowmentFund + amount)
            } else {
                it
            }
        }
        val reducedState = state.copy(
            privateBalance = state.privateBalance - amount,
            foundations = nextFoundations
        )
        val fName = state.foundations.find { it.id == foundationId }?.name ?: "Yayasan"
        _playerState.value = logToPrivateLedger(reducedState, "Suntik Dana Abadi: $fName", amount, false)
        saveState(_playerState.value)
        return true
    }

    fun buildFoundationFacility(
        foundationId: String,
        name: String,
        category: String,
        tier: String,
        buildCost: Long,
        buildMonths: Int,
        monthlyOps: Long,
        prestigeReward: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        if (foundation.endowmentFund < buildCost) return false

        val newFacility = com.example.data.FoundationFacility(
            name = name,
            category = category,
            tier = tier,
            buildCost = buildCost,
            buildMonthsLeft = buildMonths,
            monthlyOperationalCost = monthlyOps,
            prestigeReward = prestigeReward,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map {
            if (it.id == foundationId) {
                it.copy(
                    endowmentFund = it.endowmentFund - buildCost,
                    facilities = it.facilities + newFacility
                )
            } else {
                it
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildEducationInstitution(foundationId: String, name: String, level: String): Boolean {
        return buildEducationInstitution(foundationId, name, level, "Grade A", 0L)
    }

    fun buildEducationInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        
        val cost = when (level) {
            "TK" -> 200000L
            "SD" -> 500000L
            "SMA" -> 1500000L
            "UNIV" -> 5000000L
            else -> 200000L
        }
        
        if (foundation.endowmentFund < cost) return false
        
        val basePrestige = when (level) {
            "TK" -> 5
            "SD" -> 15
            "SMA" -> 40
            "UNIV" -> 100
            else -> 5
        }
        
        val defaultCurriculum = if (level == "UNIV") "Nasional (Teaching Univ)" else "Nasional"
        
        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 0

        val newInst = com.example.data.EducationInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            curriculumType = defaultCurriculum,
            facilityLevel = 1,
            accreditationPoints = 0,
            monthlyOperationalCost = com.example.data.calculateEduOperationalCost(level, 1, defaultCurriculum),
            prestigeScore = basePrestige,
            imageUrl = "",
            currentStudents = 0,
            monthlySpp = 0L,
            buildingGrade = buildingGrade,
            baseMaintenanceCost = baseMaintenanceCost,
            additionalFacilities = emptyList(),
            constructionMonthsTotal = duration,
            constructionMonthsLeft = duration,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    educationInstitutions = (f.educationInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateEducationInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val randomStudents = when (inst.level) {
            "TK" -> (100..300).random()
            "SD" -> (200..600).random()
            "SMA" -> (300..800).random()
            "UNIV" -> (5000..15000).random()
            else -> 0
        }
        
        val updatedInst = inst.copy(
            isOperational = true,
            currentStudents = randomStudents
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateEducationInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.educationInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(educationInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeEduFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "TK" -> 150000L
            "SD" -> 400000L
            "SMA" -> 1200000L
            "UNIV" -> 4000000L
            else -> 150000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "TK" -> 5
            "SD" -> 15
            "SMA" -> 40
            "UNIV" -> 100
            else -> 5
        }
        val nextPrestige = basePrestige * nextLevel
        val nextOps = com.example.data.calculateEduOperationalCost(inst.level, nextLevel, inst.curriculumType)

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige,
            monthlyOperationalCost = nextOps
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeEduCurriculum(foundationId: String, institutionId: String, newCurriculum: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextStudents = if (inst.level == "TK") {
            when (newCurriculum) {
                "Nasional" -> (100..300).random()
                "Montessori" -> (60..90).random()
                "Waldorf" -> (30..40).random()
                else -> (100..300).random()
            }
        } else if (inst.level == "SD") {
            when (newCurriculum) {
                "Nasional" -> (200..600).random()
                "Agama Terpadu" -> (150..450).random()
                "Nasional Plus (Bilingual)" -> (100..300).random()
                "Cambridge Primary" -> (80..200).random()
                else -> (200..600).random()
            }
        } else if (inst.level == "SMA") {
            when (newCurriculum) {
                "Nasional" -> (300..800).random()
                "Kejuruan (SMK)" -> (250..600).random()
                "Cambridge (A-Level)" -> (150..400).random()
                "IB (International Baccalaureate)" -> (100..300).random()
                else -> (300..800).random()
            }
        } else if (inst.level == "UNIV") {
            when (newCurriculum) {
                "Nasional (Teaching Univ)" -> (5000..15000).random()
                "Internasional (Double Degree)" -> (3000..8000).random()
                "World-Class Research Univ" -> (1000..4000).random()
                else -> (5000..15000).random()
            }
        } else {
            inst.currentStudents
        }

        val nextOps = com.example.data.calculateEduOperationalCost(inst.level, inst.facilityLevel, newCurriculum)
        val updatedInst = inst.copy(
            curriculumType = newCurriculum,
            monthlyOperationalCost = nextOps,
            currentStudents = nextStudents
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteEducationInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        val updatedState = state.copy(foundations = nextFoundations)
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Fasilitas: ${inst.name} (${inst.level})", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun updateEducationInstitutionSpp(foundationId: String, institutionId: String, newSpp: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedInst = inst.copy(monthlySpp = newSpp)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateInstitutionCurriculum(foundationId: String, institutionId: String, newCurriculum: String): Boolean {
        return changeEduCurriculum(foundationId, institutionId, newCurriculum)
    }

    fun updateInstitutionCurriculum(institutionId: String, newCurriculum: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.educationInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return changeEduCurriculum(foundation.id, institutionId, newCurriculum)
    }

    fun updateInstitutionSpp(foundationId: String, institutionId: String, inputSpp: Long): Boolean {
        return updateEducationInstitutionSpp(foundationId, institutionId, inputSpp)
    }

    fun updateInstitutionSpp(institutionId: String, inputSpp: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.educationInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return updateEducationInstitutionSpp(foundation.id, institutionId, inputSpp)
    }

    fun buildAdditionalFacility(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        
        // Jelas jika saldo tidak cukup
        if (foundation.endowmentFund < constructionCost) {
            return false
        }
        
        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )
        
        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.educationInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + when (typeId) {
                                "tk_playground" -> 8
                                "tk_pool" -> 5
                                "tk_computer_lab" -> 12
                                "tk_nap_room" -> 6
                                else -> 5
                            },
                            accreditationPoints = Math.min(100, inst.accreditationPoints + when (typeId) {
                                "tk_playground" -> 5
                                "tk_pool" -> 2
                                "tk_computer_lab" -> 10
                                "tk_nap_room" -> 4
                                else -> 3
                            })
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    educationInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireTeacher(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedTeachers = when (type) {
            "umum" -> inst.teachers.copy(umum = inst.teachers.umum.copy(
                target = inst.teachers.umum.target + 1,
                recruiting = inst.teachers.umum.recruiting + 1
            ))
            "spesialis" -> inst.teachers.copy(spesialis = inst.teachers.spesialis.copy(
                target = inst.teachers.spesialis.target + 1,
                recruiting = inst.teachers.spesialis.recruiting + 1
            ))
            "senior" -> inst.teachers.copy(senior = inst.teachers.senior.copy(
                target = inst.teachers.senior.target + 1,
                recruiting = inst.teachers.senior.recruiting + 1
            ))
            else -> inst.teachers
        }
        
        val updatedInst = inst.copy(teachers = updatedTeachers)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireTeacher(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedTeachers = when (type) {
            "umum" -> {
                val r = inst.teachers.umum
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(umum = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "spesialis" -> {
                val r = inst.teachers.spesialis
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(spesialis = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "senior" -> {
                val r = inst.teachers.senior
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(senior = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.teachers
        }
        
        val updatedInst = inst.copy(teachers = updatedTeachers)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(
                target = inst.supportStaff.ob.target + 1,
                recruiting = inst.supportStaff.ob.recruiting + 1
            ))
            "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(
                target = inst.supportStaff.satpam.target + 1,
                recruiting = inst.supportStaff.satpam.recruiting + 1
            ))
            "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(
                target = inst.supportStaff.admin.target + 1,
                recruiting = inst.supportStaff.admin.recruiting + 1
            ))
            "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(
                target = inst.supportStaff.chef.target + 1,
                recruiting = inst.supportStaff.chef.recruiting + 1
            ))
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> {
                val r = inst.supportStaff.ob
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(ob = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "security" -> {
                val r = inst.supportStaff.satpam
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(satpam = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "admin" -> {
                val r = inst.supportStaff.admin
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(admin = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "chef" -> {
                val r = inst.supportStaff.chef
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(chef = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateStaffSalary(
        foundationId: String,
        institutionId: String,
        isTeacher: Boolean,
        roleType: String,
        newSalary: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedInst = if (isTeacher) {
            val updatedTeachers = when (roleType) {
                "umum" -> inst.teachers.copy(umum = inst.teachers.umum.copy(customSalary = newSalary))
                "spesialis" -> inst.teachers.copy(spesialis = inst.teachers.spesialis.copy(customSalary = newSalary))
                "senior" -> inst.teachers.copy(senior = inst.teachers.senior.copy(customSalary = newSalary))
                else -> inst.teachers
            }
            inst.copy(teachers = updatedTeachers)
        } else {
            val updatedSupport = when (roleType) {
                "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(customSalary = newSalary))
                "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(customSalary = newSalary))
                "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(customSalary = newSalary))
                "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(customSalary = newSalary))
                else -> inst.supportStaff
            }
            inst.copy(supportStaff = updatedSupport)
        }
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameFacility(foundationId: String, institutionId: String, facilityId: String, newName: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) {
                fac.copy(customName = newName)
            } else {
                fac
            }
        }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteFacility(foundationId: String, institutionId: String, facilityId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildHealthInstitution(foundationId: String, name: String, level: String): Boolean {
        return buildHealthInstitution(foundationId, name, level, "Grade A", 0L)
    }

    fun buildHealthInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        
        val cost = when (level) {
            "Klinik" -> 300000L
            "RS Umum" -> 1000000L
            "RS Khusus" -> 3000000L
            "RS Internasional" -> 10000000L
            else -> 300000L
        }
        
        if (foundation.endowmentFund < cost) return false
        
        val basePrestige = when (level) {
            "Klinik" -> 8
            "RS Umum" -> 25
            "RS Khusus" -> 70
            "RS Internasional" -> 180
            else -> 8
        }
        
        val defaultServiceType = "Reguler"
        
        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 0

        val newInst = com.example.data.HealthInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            serviceType = defaultServiceType,
            facilityLevel = 1,
            accreditationPoints = 0,
            monthlyOperationalCost = 0L,
            prestigeScore = basePrestige,
            imageUrl = "",
            currentPatients = 0,
            monthlyBillPerPatient = 0L,
            buildingGrade = buildingGrade,
            baseMaintenanceCost = baseMaintenanceCost,
            additionalFacilities = emptyList(),
            constructionMonthsTotal = duration,
            constructionMonthsLeft = duration,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    healthInstitutions = (f.healthInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateHealthInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val randomPatients = when (inst.level) {
            "Klinik" -> (15..50).random()
            "RS Umum" -> (80..200).random()
            "RS Khusus" -> (120..350).random()
            "RS Internasional" -> (200..600).random()
            else -> 0
        }
        
        val updatedInst = inst.copy(
            isOperational = true,
            currentPatients = randomPatients
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.healthInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(healthInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeHealthFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "Klinik" -> 250000L
            "RS Umum" -> 800000L
            "RS Khusus" -> 2500000L
            "RS Internasional" -> 8000000L
            else -> 250000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "Klinik" -> 8
            "RS Umum" -> 25
            "RS Khusus" -> 70
            "RS Internasional" -> 180
            else -> 8
        }
        val nextPrestige = basePrestige * nextLevel

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeHealthServiceType(foundationId: String, institutionId: String, newServiceType: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextPatients = when (newServiceType) {
            "Subsidi" -> (50..150).random()
            "Reguler" -> (30..100).random()
            "VIP" -> (15..45).random()
            "VVIP" -> (5..20).random()
            else -> (30..100).random()
        }

        val updatedInst = inst.copy(
            serviceType = newServiceType,
            currentPatients = nextPatients
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteHealthInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        val updatedState = state.copy(foundations = nextFoundations)
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Layanan Medis: ${inst.name} (${inst.level})", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthInstitutionBill(foundationId: String, institutionId: String, newBill: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedInst = inst.copy(monthlyBillPerPatient = newBill)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthBill(institutionId: String, inputBill: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.healthInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return updateHealthInstitutionBill(foundation.id, institutionId, inputBill)
    }

    fun buildHealthAdditionalFacility(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        
        if (foundation.endowmentFund < constructionCost) {
            return false
        }
        
        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )
        
        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.healthInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + 10,
                            accreditationPoints = Math.min(100, inst.accreditationPoints + 8)
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    healthInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireMedicalStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedMedical = when (type) {
            "perawat" -> inst.medicalStaff.copy(perawat = inst.medicalStaff.perawat.copy(
                target = inst.medicalStaff.perawat.target + 1,
                recruiting = inst.medicalStaff.perawat.recruiting + 1
            ))
            "dokterUmum" -> inst.medicalStaff.copy(dokterUmum = inst.medicalStaff.dokterUmum.copy(
                target = inst.medicalStaff.dokterUmum.target + 1,
                recruiting = inst.medicalStaff.dokterUmum.recruiting + 1
            ))
            "dokterSpesialis" -> inst.medicalStaff.copy(dokterSpesialis = inst.medicalStaff.dokterSpesialis.copy(
                target = inst.medicalStaff.dokterSpesialis.target + 1,
                recruiting = inst.medicalStaff.dokterSpesialis.recruiting + 1
            ))
            else -> inst.medicalStaff
        }
        
        val updatedInst = inst.copy(medicalStaff = updatedMedical)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireMedicalStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedMedical = when (type) {
            "perawat" -> {
                val r = inst.medicalStaff.perawat
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(perawat = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "dokterUmum" -> {
                val r = inst.medicalStaff.dokterUmum
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(dokterUmum = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "dokterSpesialis" -> {
                val r = inst.medicalStaff.dokterSpesialis
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(dokterSpesialis = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.medicalStaff
        }
        
        val updatedInst = inst.copy(medicalStaff = updatedMedical)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireHealthSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(
                target = inst.supportStaff.ob.target + 1,
                recruiting = inst.supportStaff.ob.recruiting + 1
            ))
            "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(
                target = inst.supportStaff.satpam.target + 1,
                recruiting = inst.supportStaff.satpam.recruiting + 1
            ))
            "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(
                target = inst.supportStaff.admin.target + 1,
                recruiting = inst.supportStaff.admin.recruiting + 1
            ))
            "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(
                target = inst.supportStaff.chef.target + 1,
                recruiting = inst.supportStaff.chef.recruiting + 1
            ))
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireHealthSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> {
                val r = inst.supportStaff.ob
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(ob = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "security" -> {
                val r = inst.supportStaff.satpam
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(satpam = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "admin" -> {
                val r = inst.supportStaff.admin
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(admin = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "chef" -> {
                val r = inst.supportStaff.chef
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(chef = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthStaffSalary(
        foundationId: String,
        institutionId: String,
        isMedical: Boolean,
        roleType: String,
        newSalary: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedInst = if (isMedical) {
            val updatedMedical = when (roleType) {
                "perawat" -> inst.medicalStaff.copy(perawat = inst.medicalStaff.perawat.copy(customSalary = newSalary))
                "dokterUmum" -> inst.medicalStaff.copy(dokterUmum = inst.medicalStaff.dokterUmum.copy(customSalary = newSalary))
                "dokterSpesialis" -> inst.medicalStaff.copy(dokterSpesialis = inst.medicalStaff.dokterSpesialis.copy(customSalary = newSalary))
                else -> inst.medicalStaff
            }
            inst.copy(medicalStaff = updatedMedical)
        } else {
            val updatedSupport = when (roleType) {
                "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(customSalary = newSalary))
                "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(customSalary = newSalary))
                "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(customSalary = newSalary))
                "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(customSalary = newSalary))
                else -> inst.supportStaff
            }
            inst.copy(supportStaff = updatedSupport)
        }
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameHealthFacility(foundationId: String, institutionId: String, facilityId: String, newName: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) {
                fac.copy(customName = newName)
            } else {
                fac
            }
        }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteHealthFacility(foundationId: String, institutionId: String, facilityId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildCharityInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false

        val cost = when (level) {
            "Humanitarian Aid" -> 150000L
            "Social Care" -> 300000L
            "Disaster Relief" -> 800000L
            "Community Empowerment" -> 500000L
            else -> 150000L
        }

        if (foundation.endowmentFund < cost) return false

        val basePrestige = when (level) {
            "Humanitarian Aid" -> 10
            "Social Care" -> 20
            "Disaster Relief" -> 60
            "Community Empowerment" -> 40
            else -> 10
        }

        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 1

        val newInst = com.example.data.CharityInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            scope = "Lokal",
            facilityLevel = 1,
            accreditationPoints = 0,
            prestigeScore = basePrestige,
            imageUrl = "",
            baseMaintenanceCost = baseMaintenanceCost,
            constructionTotalMonths = duration,
            constructionLeftMonths = duration,
            isOperational = false,
            monthlyBeneficiaries = 0,
            maxCapacity = com.example.data.calculateCharityMaxCapacity(level, "Lokal"),
            additionalFacilities = emptyList(),
            charityStaff = com.example.data.CharityStaff(),
            buildingGrade = buildingGrade
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    charityInstitutions = (f.charityInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }

        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateCharityInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val initialBeneficiaries = when (inst.level) {
            "Humanitarian Aid" -> (10..50).random()
            "Social Care" -> (5..20).random()
            "Disaster Relief" -> (20..80).random()
            "Community Empowerment" -> (10..30).random()
            else -> 5
        }

        val updatedInst = inst.copy(
            isOperational = true,
            monthlyBeneficiaries = initialBeneficiaries
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeCharityScope(foundationId: String, institutionId: String, newScope: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextCapacity = com.example.data.calculateCharityMaxCapacity(inst.level, newScope)

        val updatedInst = inst.copy(
            scope = newScope,
            maxCapacity = nextCapacity
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteCharityInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeCharityInstitutionFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "Humanitarian Aid" -> 200000L
            "Social Care" -> 400000L
            "Disaster Relief" -> 1000000L
            "Community Empowerment" -> 600000L
            else -> 200000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "Humanitarian Aid" -> 15
            "Social Care" -> 30
            "Disaster Relief" -> 90
            "Community Empowerment" -> 60
            else -> 15
        }
        val nextPrestige = basePrestige * nextLevel

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireCharityStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> inst.charityStaff.copy(relawan = inst.charityStaff.relawan.copy(
                target = inst.charityStaff.relawan.target + 1,
                recruiting = inst.charityStaff.relawan.recruiting + 1
            ))
            "staffSosial" -> inst.charityStaff.copy(staffSosial = inst.charityStaff.staffSosial.copy(
                target = inst.charityStaff.staffSosial.target + 1,
                recruiting = inst.charityStaff.staffSosial.recruiting + 1
            ))
            "ahliProgram" -> inst.charityStaff.copy(ahliProgram = inst.charityStaff.ahliProgram.copy(
                target = inst.charityStaff.ahliProgram.target + 1,
                recruiting = inst.charityStaff.ahliProgram.recruiting + 1
            ))
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireCharityStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> {
                val r = inst.charityStaff.relawan
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(relawan = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "staffSosial" -> {
                val r = inst.charityStaff.staffSosial
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(staffSosial = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "ahliProgram" -> {
                val r = inst.charityStaff.ahliProgram
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(ahliProgram = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateCharityStaffSalary(foundationId: String, institutionId: String, type: String, newSalary: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> inst.charityStaff.copy(relawan = inst.charityStaff.relawan.copy(customSalary = newSalary))
            "staffSosial" -> inst.charityStaff.copy(staffSosial = inst.charityStaff.staffSosial.copy(customSalary = newSalary))
            "ahliProgram" -> inst.charityStaff.copy(ahliProgram = inst.charityStaff.ahliProgram.copy(customSalary = newSalary))
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun addCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false

        if (foundation.endowmentFund < constructionCost) {
            return false
        }

        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )

        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.charityInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + 10,
                            accreditationPoints = Math.min(100, inst.accreditationPoints + 5)
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    charityInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }

        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        facilityId: String,
        newName: String
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) fac.copy(customName = newName) else fac
        }

        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        facilityId: String
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }

        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateCharityInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.charityInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(charityInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }
}