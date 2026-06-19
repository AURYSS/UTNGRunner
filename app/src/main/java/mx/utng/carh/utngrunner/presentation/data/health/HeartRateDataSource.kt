package mx.utng.carh.utngrunner.presentation.data.health

import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeartRateDataSource(private val healthServicesClient: HealthServicesClient) {

    private val _heartRate = MutableStateFlow(72)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    fun startMonitoring() {
        val client = healthServicesClient.passiveMonitoringClient
        val config = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()
        client.setPassiveListenerCallback(config, object : PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val bpm = dataPoints
                    .getData(DataType.HEART_RATE_BPM)
                    .lastOrNull()?.value?.toInt() ?: return
                _heartRate.value = bpm
            }
        })
    }
}
