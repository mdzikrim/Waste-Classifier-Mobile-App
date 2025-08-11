package com.example.tes.ui.profil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tes.R
import com.example.tes.data.AppDatabase
import com.example.tes.data.SampahEntity
import com.example.tes.ui.identifikasi.IdentifikasiActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfilActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        // Set Data User
        val namaUser = findViewById<TextView>(R.id.namaUser)
        val statusUser = findViewById<TextView>(R.id.statusUser)
        val statistikUser = findViewById<TextView>(R.id.statistikUser)
        val fotoUser = findViewById<ImageView>(R.id.imageView)
        namaUser.text = "Nama User"
        statusUser.text = "Status User"

        // Chart
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        recyclerView = findViewById(R.id.recyclerViewHistory)

        // Ambil data dari Room DB & update chart
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).sampahDao()
            val data = dao.getAll()
            runOnUiThread {
                statistikUser.text = "Jumlah Sampah Dibuang: ${data.size}"
                setupPieChart(data)
                setupBarChart(data)
                loadHistory(data)
            }
        }

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.item_profil
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_identifikasi -> {
                    startActivity(Intent(this, IdentifikasiActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.item_profil -> true
                else -> false
            }
        }
    }

    private fun setupPieChart(data: List<SampahEntity>) {
        val organikCount = data.count { it.labelLevel1.equals("Organik", ignoreCase = true) }
        val anorganikCount = data.count { it.labelLevel1.equals("Anorganik", ignoreCase = true) }

        val entries = listOf(
            PieEntry(organikCount.toFloat(), "Organik"),
            PieEntry(anorganikCount.toFloat(), "Anorganik")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            ContextCompat.getColor(this, R.color.colorOrganik),
            ContextCompat.getColor(this, R.color.colorAnorganik)
        )
        dataSet.valueTextSize = 15f
        dataSet.sliceSpace = 3f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.setDrawHoleEnabled(false)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.invalidate()
    }

    private fun setupBarChart(data: List<SampahEntity>) {
        val hariList = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val cal = Calendar.getInstance()
        val hariIniIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // Tampil 7 Hari Kebelakang di BarChart
        val tanggal7Hari = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 6 downTo 0) {
            val clone = cal.clone() as Calendar
            clone.add(Calendar.DAY_OF_YEAR, -i)
            tanggal7Hari.add(sdf.format(clone.time))
        }

        // Rotasi Hari
        val orderedDays = mutableListOf<String>()
        for (i in 0 until 7) {
            orderedDays.add(hariList[(hariIniIndex - 6 + i + 7) % 7])
        }

        // Hitung jumlah organik & anorganik untuk masing-masing hari
        val organikData = mutableListOf<Int>()
        val anorganikData = mutableListOf<Int>()
        for (tanggal in tanggal7Hari) {
            val organik = data.count {
                it.labelLevel1.equals("Organik", ignoreCase = true)
                        && it.timestamp.startsWith(tanggal)
            }
            val anorganik = data.count {
                it.labelLevel1.equals("Anorganik", ignoreCase = true)
                        && it.timestamp.startsWith(tanggal)
            }
            organikData.add(organik)
            anorganikData.add(anorganik)
        }

        val groupCount = 7
        val barWidth = 0.4f
        val barSpace = 0.05f
        val groupSpace = 0.12f

        val barEntriesOrganik = ArrayList<BarEntry>()
        val barEntriesAnorganik = ArrayList<BarEntry>()

        for (i in 0 until groupCount) {
            barEntriesOrganik.add(BarEntry(i.toFloat(), organikData[i].toFloat()))
            barEntriesAnorganik.add(BarEntry(i.toFloat(), anorganikData[i].toFloat()))
        }

        val barDataSetOrganik = BarDataSet(barEntriesOrganik, "Organik")
        barDataSetOrganik.color = ContextCompat.getColor(this, R.color.colorOrganik)
        val barDataSetAnorganik = BarDataSet(barEntriesAnorganik, "Anorganik")
        barDataSetAnorganik.color = ContextCompat.getColor(this, R.color.colorAnorganik)

        val barData = BarData(barDataSetOrganik, barDataSetAnorganik)
        barData.barWidth = barWidth

        barChart.data = barData
        barChart.xAxis.apply {
            granularity = 1f
            isGranularityEnabled = true
            setCenterAxisLabels(true)
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(orderedDays)
            setDrawGridLines(false)
            textSize = 12f
            labelCount = groupCount
        }
        barChart.axisLeft.apply {
            granularity = 1f
            axisMinimum = 0f
            setDrawGridLines(true)
            textSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(groupCount.toFloat())
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.legend.isEnabled = true

        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.invalidate()
    }

    private fun loadHistory(data: List<SampahEntity>) {
        val sortedData = data.sortedByDescending { it.timestamp }
        val fiveLast = if (sortedData.size > 5) sortedData.take(5) else sortedData
        historyAdapter = HistoryAdapter(fiveLast)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }
}
