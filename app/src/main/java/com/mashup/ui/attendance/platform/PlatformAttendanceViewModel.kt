package com.mashup.ui.attendance.platform

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.mashup.base.BaseViewModel
import com.mashup.data.dto.TotalAttendanceResponse
import com.mashup.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlatformAttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    val scheduleId
        get() = savedStateHandle.get<Int>(EXTRA_SCHEDULE_ID)

    private val _notice = mutableStateOf("")
    val notice: State<String> = _notice

    private val _platformAttendanceState = mutableStateOf<PlatformAttendanceState>(
        PlatformAttendanceState.Empty
    )
    val platformAttendanceState: State<PlatformAttendanceState> =
        _platformAttendanceState

    fun getPlatformAttendanceList() = mashUpScope {
        _platformAttendanceState.value = PlatformAttendanceState.Loading
        val scheduleId = scheduleId ?: return@mashUpScope
        val response = attendanceRepository.getPlatformAttendanceList(scheduleId)

        if (!response.isSuccess()) {
            handleErrorCode(response.code)
            return@mashUpScope
        }

        response.data?.run {
            _notice.value = when {
                eventNum == 1 && !isEnd -> {
                    "출석체크가 실시간으로 진행되고 있어요"
                }
                eventNum == 2 && !isEnd -> {
                    "중간 집계 중이에요"
                }
                eventNum == 2 && isEnd -> {
                    "출석체크가 완료되었어요"
                }
                else -> {
                    "서버에서 이상한 일이 발생했어요 ㅜ"
                }
            }

            _platformAttendanceState.value =
                PlatformAttendanceState.Success(response.data)
        }
    }

    companion object {
        const val EXTRA_SCHEDULE_ID = "EXTRA_SCHEDULE_ID"
    }

    override fun handleErrorCode(code: String) {
        mashUpScope {
            _platformAttendanceState.value = PlatformAttendanceState.Error(code)
        }
    }
}

sealed interface PlatformAttendanceState {
    object Empty : PlatformAttendanceState
    object Loading : PlatformAttendanceState
    data class Success(val data: TotalAttendanceResponse) : PlatformAttendanceState
    data class Error(val code: String) : PlatformAttendanceState
}