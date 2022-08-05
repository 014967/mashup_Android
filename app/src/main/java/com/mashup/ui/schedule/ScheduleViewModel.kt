package com.mashup.ui.schedule

import com.mashup.base.BaseViewModel
import com.mashup.data.datastore.UserDataSource
import com.mashup.data.dto.ScheduleResponse
import com.mashup.data.dto.SchedulesProgress
import com.mashup.data.repository.AttendanceRepository
import com.mashup.data.repository.ScheduleRepository
import com.mashup.network.errorcode.UNAUTHORIZED
import com.mashup.ui.schedule.model.ScheduleCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val userDataSource: UserDataSource,
    private val scheduleRepository: ScheduleRepository,
    private val attendanceRepository: AttendanceRepository
) : BaseViewModel() {
    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Empty)
    val scheduleState: StateFlow<ScheduleState> = _scheduleState

    init {
        getScheduleList()
    }

    fun getScheduleList() {
        mashUpScope {
            val generateNumber = userDataSource.generateNumber
            if (generateNumber == null) {
                handleErrorCode(UNAUTHORIZED)
                return@mashUpScope
            }

            val response = scheduleRepository.getScheduleList(generateNumber)

            if (!response.isSuccess() || response.data == null) {
                handleErrorCode(response.code)
                return@mashUpScope
            }
            _scheduleState.emit(
                ScheduleState.Success(
                    scheduleTitleState = when (response.data.progress) {
                        SchedulesProgress.DONE -> {
                            ScheduleTitleState.End(generateNumber)
                        }
                        SchedulesProgress.NOT_REGISTERED -> {
                            ScheduleTitleState.Empty
                        }
                        SchedulesProgress.ON_GOING -> {
                            ScheduleTitleState.DateCount(response.data.dateCount)
                        }
                    },
                    scheduleList = if (response.data.scheduleList.isEmpty()) {
                        listOf(ScheduleCard.EmptySchedule())
                    } else {
                        response.data.scheduleList.map { mapperToScheduleCard(it) }
                    }
                )
            )
        }
    }

    override fun handleErrorCode(code: String) {
        mashUpScope {
            _scheduleState.emit(ScheduleState.Error(code))
        }
    }

    private suspend fun mapperToScheduleCard(scheduleResponse: ScheduleResponse): ScheduleCard {
        if (scheduleResponse.eventList.isEmpty()) {
            return ScheduleCard.EmptySchedule(scheduleResponse)
        }

        val attendResponse = attendanceRepository.getScheduleAttendanceInfo(
            scheduleResponse.scheduleId
        )

        return when {
            !attendResponse.isSuccess() || attendResponse.data == null
                || attendResponse.data.attendanceInfos.size < 2 -> {
                ScheduleCard.InProgressSchedule(
                    scheduleResponse = scheduleResponse,
                    attendanceInfo = attendResponse.data
                )
            }
            else -> {
                ScheduleCard.EndSchedule(
                    scheduleResponse = scheduleResponse,
                    attendanceInfo = attendResponse.data
                )
            }
        }
    }
}

sealed interface ScheduleTitleState {
    data class DateCount(val dataCount: Int) : ScheduleTitleState
    data class End(val generatedNumber: Int) : ScheduleTitleState
    object Empty : ScheduleTitleState
}

sealed interface ScheduleState {
    object Empty : ScheduleState
    object Loading : ScheduleState
    data class Success(
        val scheduleTitleState: ScheduleTitleState,
        val scheduleList: List<ScheduleCard>
    ) : ScheduleState

    data class Error(val code: String) : ScheduleState
}