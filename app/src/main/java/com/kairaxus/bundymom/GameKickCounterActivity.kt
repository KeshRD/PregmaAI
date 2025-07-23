package com.kairaxus.bundymom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import java.text.SimpleDateFormat
import java.util.*

data class KickRecord(
    val count: Int = 0,
    val date: String = "",
    val startTime: String = "",
    val endTime: String? = null,
    val duration: String? = null,
    val coinsEarned: Int = 0,
    val highestLevel: Int = 0,
    val endPosition: Int = 0
)

data class BabyCostume(
    val id: Int,
    val name: String,
    val price: Int,
    val imageRes: Int,
    val purchased: Boolean = false,
    val equipped: Boolean = false
)

class GameKickCounterActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BundyMomTheme {
                EnhancedGamifiedKickCounterScreen(
                    auth = FirebaseAuth.getInstance(),
                    db = FirebaseFirestore.getInstance(),
                    onBackClicked = {
                        // Handle back navigation
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                )
            }


        }
    }
}

@Composable
fun EnhancedGamifiedKickCounterScreen(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onBackClicked: () -> Unit
) {
    // Game state variables
    var kickCount by remember { mutableStateOf(0) }
    var sessionStartTime by remember { mutableStateOf("") }
    var sessionEndTime by remember { mutableStateOf<String?>(null) }
    var sessionDuration by remember { mutableStateOf<String?>(null) }
    var isSessionActive by remember { mutableStateOf(false) }
    var records by remember { mutableStateOf<List<KickRecord>>(emptyList()) }
    var totalCoins by remember { mutableStateOf(0) }
    var currentLevel by remember { mutableStateOf(1) }
    var babyPosition by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) }

    // Costume state with guaranteed default
    var costumes by remember {
        mutableStateOf(
            listOf(
                BabyCostume(1, "Basic Baby", 0, R.drawable.babymain, true, true)
            )
        )
    }
    var currentCostume by remember {
        mutableStateOf(
            costumes.firstOrNull { it.equipped }
                ?: costumes.firstOrNull()
                ?: BabyCostume(1, "Basic Baby", 0, R.drawable.babymain, true, true)
        )
    }

    // Animation states
    val animatedBabyPosition by animateDpAsState(
        targetValue = (babyPosition % 800).dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f)
    )
    val rotationAngle by animateFloatAsState(
        targetValue = if (isSessionActive) (kickCount * 120f) % 360f else 0f, // Changed from 5f to 180f
        animationSpec = tween(durationMillis = 10000) // Faster animation (500ms → 200ms)
    )

    // Date/time formatters
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Background images
    val backgroundImages = listOf(
        R.drawable.sky1,
        R.drawable.sky2,
        R.drawable.sky3,
        R.drawable.sky4
    )
    val currentBackground by derivedStateOf {
        backgroundImages[(currentLevel - 1).coerceIn(0, backgroundImages.size - 1)]
    }

    // Load data from Firestore
    LaunchedEffect(Unit) {
        auth.currentUser?.email?.let { email ->
            // Load kick records
            db.collection("users").document(email)
                .collection("kickRecords")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    records = documents.mapNotNull { doc ->
                        doc.toObject(KickRecord::class.java)
                    }
                    records.firstOrNull()?.endPosition?.let { pos ->
                        babyPosition = pos
                    }
                }

            // Load user data with fallbacks
            db.collection("users").document(email)
                .get()
                .addOnSuccessListener { document ->
                    totalCoins = document.getLong("totalCoins")?.toInt() ?: 0
                    babyPosition = document.getLong("lastPosition")?.toInt() ?: 0
                    currentLevel = document.getLong("lastLevel")?.toInt() ?: 1


                    val purchasedCostumes = document.get("purchasedCostumes") as? List<Int> ?: listOf(1) // Default includes basic baby
                    val equippedCostumeId = document.getLong("equippedCostume")?.toInt() ?: 1 // Default to basic baby

                    costumes = buildList {
                        // Always include basic baby first
                        add(
                            BabyCostume(
                                id = 1,
                                name = "Basic Baby",
                                price = 0,
                                imageRes = R.drawable.babymain,
                                purchased = true,
                                equipped = equippedCostumeId == 1
                            )
                        )
                        // Add other costumes
                        addAll(
                            listOf(
                                BabyCostume(2, "Super Baby", 100, R.drawable.superbab,
                                    purchased = purchasedCostumes.contains(2),
                                    equipped = equippedCostumeId == 2),
                                BabyCostume(3, "Astronaut Baby", 10000, R.drawable.astrobaby,
                                    purchased = purchasedCostumes.contains(3),
                                    equipped = equippedCostumeId == 3),
                                BabyCostume(4, "Pirate Baby", 1500, R.drawable.piratebaby,
                                    purchased = purchasedCostumes.contains(4),
                                    equipped = equippedCostumeId == 4),
                                BabyCostume(5, "Dinosaur Baby", 50000, R.drawable.babymain,
                                    purchased = purchasedCostumes.contains(5),
                                    equipped = equippedCostumeId == 5)
                            )
                        )
                    }

                    // Ensure exactly one costume is equipped
                    if (costumes.none { it.equipped }) {
                        costumes = costumes.mapIndexed { index, costume ->
                            if (index == 0) costume.copy(equipped = true) else costume
                        }
                    }

                    currentCostume = costumes.first { it.equipped }
                }
                .addOnFailureListener {
                    // Fallback to default if Firestore fails
                    costumes = listOf(
                        BabyCostume(1, "Basic Baby", 0, R.drawable.babymain, true, true)
                    )
                    currentCostume = costumes.first()
                }
        }
    }

    fun startNewSession() {
        kickCount = 0
        currentLevel = 1
        sessionStartTime = timeFormat.format(Date())
        sessionEndTime = null
        sessionDuration = null
        isSessionActive = true
    }

    fun endCurrentSession() {
        val endTime = Date()
        sessionEndTime = timeFormat.format(endTime)
        val startTime = timeFormat.parse(sessionStartTime)?.time ?: 0
        val durationMillis = endTime.time - startTime
        val minutes = (durationMillis / (1000 * 60)) % 60
        val seconds = (durationMillis / 1000) % 60
        sessionDuration = String.format("%02d:%02d", minutes, seconds)
        isSessionActive = false

        val coinsEarned = (kickCount * 10) + (currentLevel * 50)
        totalCoins += coinsEarned

        auth.currentUser?.email?.let { email ->
            val record = KickRecord(
                count = kickCount,
                date = currentDate,
                startTime = sessionStartTime,
                endTime = sessionEndTime,
                duration = sessionDuration,
                coinsEarned = coinsEarned,
                highestLevel = currentLevel,
                endPosition = babyPosition
            )

            db.collection("users").document(email)
                .collection("kickRecords")
                .add(record)
                .addOnSuccessListener {
                    records = listOf(record) + records
                }

            val updates = hashMapOf<String, Any>(
                "totalCoins" to totalCoins,
                "equippedCostume" to currentCostume.id,
                "lastPosition" to babyPosition,
                "lastLevel" to currentLevel,
                "purchasedCostumes" to costumes.filter { it.purchased }.map { it.id }
            )

            db.collection("users").document(email)
                .update(updates)
        }
    }

    fun addKick() {
        kickCount++
        babyPosition += 20
        currentLevel = (kickCount / 5) + 1
    }

    fun purchaseCostume(costume: BabyCostume) {
        if (totalCoins >= costume.price) {
            totalCoins -= costume.price
            costumes = costumes.map {
                if (it.id == costume.id) it.copy(purchased = true) else it
            }
        }
    }

    fun equipCostume(costume: BabyCostume) {
        if (costume.purchased) {
            costumes = costumes.map {
                it.copy(equipped = it.id == costume.id)
            }
            currentCostume = costumes.first { it.equipped }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Baby Kicks Adventure") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {  // Use the callback
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.gameicon),
                            contentDescription = "Game",
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    label = { Text("Game") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.charticon),
                            contentDescription = "Records",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = { Text("Records & Shop") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> GameScreen(
                    currentBackground = currentBackground,
                    totalCoins = totalCoins,
                    currentLevel = currentLevel,
                    animatedBabyPosition = animatedBabyPosition,
                    rotationAngle = rotationAngle,
                    currentCostume = currentCostume,
                    kickCount = kickCount,
                    isSessionActive = isSessionActive,
                    sessionStartTime = sessionStartTime,
                    startNewSession = ::startNewSession,
                    addKick = ::addKick,
                    endCurrentSession = ::endCurrentSession
                )
                1 -> RecordsAndShopScreen(
                    records = records,
                    costumes = costumes,
                    totalCoins = totalCoins,
                    currentCostume = currentCostume,
                    purchaseCostume = ::purchaseCostume,
                    equipCostume = ::equipCostume
                )
            }
        }
    }
}


// Rest of your composables (GameScreen, RecordsAndShopScreen, CostumeItem) remain the same
// as in the previous implementation, just make sure to use the currentCostume safely

@Composable
fun GameScreen(
    currentBackground: Int,
    totalCoins: Int,
    currentLevel: Int,
    animatedBabyPosition: Dp,
    rotationAngle: Float,
    currentCostume: BabyCostume,
    kickCount: Int,
    isSessionActive: Boolean,
    sessionStartTime: String,
    startNewSession: () -> Unit,
    addKick: () -> Unit,
    endCurrentSession: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = currentBackground),
            contentDescription = "Sky background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coin),
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalCoins",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .offset(y = 200.dp - animatedBabyPosition)
                    .rotate(rotationAngle)
            ) {
                Image(
                    painter = painterResource(id = currentCostume.imageRes),
                    contentDescription = "Baby",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Level $currentLevel",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = if (isSessionActive) "Kick to make baby fly!" else "Start a new adventure!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = kickCount.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Kicks")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { if (!isSessionActive) startNewSession() },
                            enabled = !isSessionActive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Flight")
                        }

                        Button(
                            onClick = {
                                if (isSessionActive) {
                                    addKick()
                                }
                            },
                            enabled = isSessionActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Kick"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kick!")
                        }

                        Button(
                            onClick = { endCurrentSession() },
                            enabled = isSessionActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Land")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordsAndShopScreen(
    records: List<KickRecord>,
    costumes: List<BabyCostume>,
    totalCoins: Int,
    currentCostume: BabyCostume,
    purchaseCostume: (BabyCostume) -> Unit,
    equipCostume: (BabyCostume) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (records.isNotEmpty()) {
            Text(
                text = "Last 7 Sessions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val last7Records = records.take(7)
                val maxKicks = last7Records.maxOfOrNull { it.count } ?: 1

                last7Records.forEach { record ->
                    val heightPercent = if (maxKicks > 0) record.count.toFloat() / maxKicks else 0f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height((heightPercent * 100).dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${record.count}",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.date.split("-").last(),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "Previous Adventures",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No previous adventures found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)//this is
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(record.date, fontWeight = FontWeight.Bold)
                                Text("Level ${record.highestLevel}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${record.count} kicks")
                                Text("${record.coinsEarned} coins")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration: ${record.duration ?: "N/A"}")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Baby Costumes Shop",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your coins: $totalCoins",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Current costume: ${currentCostume.name}",
            fontSize = 14.sp
        )

        LazyColumn(
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(costumes.filter { it.id != 1 }) { costume ->
                CostumeItem(
                    costume = costume,
                    totalCoins = totalCoins,
                    onPurchase = { purchaseCostume(costume) },
                    onEquip = { equipCostume(costume) }
                )
            }
        }
    }
}

@Composable
fun CostumeItem(
    costume: BabyCostume,
    totalCoins: Int,
    onPurchase: () -> Unit,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = costume.imageRes),
                    contentDescription = costume.name,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(costume.name, fontWeight = FontWeight.Bold)
                    Text("${costume.price} coins")
                }
            }

            if (costume.purchased) {
                if (costume.equipped) {
                    Text("Equipped", color = Color.Green)
                } else {
                    Button(onClick = onEquip) {
                        Text("Equip")
                    }
                }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = totalCoins >= costume.price
                ) {
                    Text("Buy")
                }
            }
        }
    }
}