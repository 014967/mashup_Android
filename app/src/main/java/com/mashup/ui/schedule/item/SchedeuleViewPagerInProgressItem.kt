package com.mashup.ui.schedule.item

import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import com.mashup.R
import com.mashup.core.common.extensions.fromHtml
import com.mashup.core.ui.colors.Brand100
import com.mashup.core.ui.colors.Gray100
import com.mashup.core.ui.colors.Gray50
import com.mashup.core.ui.colors.Gray700
import com.mashup.core.ui.typography.Body1
import com.mashup.databinding.LayoutAttendanceCoachMarkBindingImpl
import com.mashup.ui.schedule.model.ScheduleCard

@Composable
fun ScheduleViewPagerInProgressItem(
    data: ScheduleCard.InProgressSchedule,
    modifier: Modifier = Modifier,
    onClickScheduleInformation: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth().wrapContentHeight().background(
            color = Color.White,
            shape = RoundedCornerShape(20.dp)
        ).border(
            width = 1.dp,
            color = Gray100,
            shape = RoundedCornerShape(20.dp)
        ).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardInfoItem(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            dDay = data.scheduleResponse.getDDay(),
            title = data.scheduleResponse.name,
            calendar = data.scheduleResponse.getDate(),
            timeLine = data.scheduleResponse.getTimeLine(),
            location = ""
        )
        Spacer(modifier = Modifier.height(16.dp))

        ConstraintLayout {
            val (coachMark, button, schedule) = createRefs()

            Column(
                modifier = Modifier.background(color = Gray50, shape = RoundedCornerShape(16.dp))
                    .height(220.dp).padding(top = 16.dp, bottom = 20.dp, start = 20.dp, end = 20.dp).fillMaxWidth()
                    .constrainAs(schedule) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(id = com.mashup.core.common.R.drawable.img_standby),
                    contentDescription = null
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                val text = stringResource(id = R.string.description_standby_schedule)
                val spannableString = SpannableStringBuilder(
                    String.format(
                        text,
                        data.attendanceInfo?.memberName ?: "알 수 없음"
                    )
                ).toString()
                Text(
                    text = HtmlCompat.fromHtml(
                        spannableString,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).toAnnotatedString(),
                    color = Gray700,
                    style = Body1
                )
            }

            AndroidViewBinding(
                modifier = Modifier.constrainAs(coachMark) {
                    bottom.linkTo(button.top, 4.dp)
                    start.linkTo(button.start)
                    end.linkTo(button.end)
                },
                factory = LayoutAttendanceCoachMarkBindingImpl::inflate,
                update = {
                    this.root.visibility = View.VISIBLE
                }
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(48.dp).background(
                    color = Brand100,
                    shape = RoundedCornerShape(16.dp)
                ).constrainAs(button) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(schedule.bottom, 12.dp)
                },
                factory = { context ->
                    AppCompatTextView(context).apply {
                        text = context.getString(R.string.click_attendance_list)
                        setTextAppearance(
                            com.mashup.core.common.R.style.TextAppearance_Mashup_Body3_14_M
                        )
                        gravity = Gravity.CENTER
                        setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                com.mashup.core.common.R.color.brand500,
                                null
                            )
                        )
                        setPadding(12, 0, 0, 0)
                        setOnClickListener {
                            onClickScheduleInformation(data.scheduleResponse.scheduleId)
                        }
                    }
                }
            )
        }
    }
}
