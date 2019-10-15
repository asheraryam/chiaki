/*
 * This file is part of Chiaki.
 *
 * Chiaki is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chiaki is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chiaki.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.metallic.chiaki.regist

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.metallic.chiaki.R
import com.metallic.chiaki.lib.RegistInfo
import kotlinx.android.synthetic.main.activity_regist_execute.*
import kotlin.math.max

class RegistExecuteActivity: AppCompatActivity()
{
	companion object
	{
		const val EXTRA_REGIST_INFO = "regist_info"

		const val RESULT_FAILED = Activity.RESULT_FIRST_USER
	}

	private lateinit var viewModel: RegistExecuteViewModel

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_regist_execute)

		viewModel = ViewModelProviders.of(this).get(RegistExecuteViewModel::class.java)

		logTextView.setHorizontallyScrolling(true)
		logTextView.movementMethod = ScrollingMovementMethod()
		viewModel.logText.observe(this, Observer {
			logTextView.text = it
			val scrollY = logTextView.layout.getLineBottom(logTextView.lineCount - 1) - logTextView.height + logTextView.paddingTop + logTextView.paddingBottom
			logTextView.scrollTo(0, max(scrollY, 0))
		})

		viewModel.state.observe(this, Observer {
			progressBar.visibility = if(it == RegistExecuteViewModel.State.RUNNING) View.VISIBLE else View.GONE
			when(it)
			{
				RegistExecuteViewModel.State.FAILED ->
				{
					infoTextView.visibility = View.VISIBLE
					infoTextView.setText(R.string.regist_info_failed)
					setResult(RESULT_FAILED)
				}
				RegistExecuteViewModel.State.SUCCESSFUL ->
				{
					infoTextView.visibility = View.VISIBLE
					infoTextView.setText(R.string.regist_info_success)
					setResult(RESULT_OK)
				}
				RegistExecuteViewModel.State.STOPPED ->
				{
					infoTextView.visibility = View.GONE
					setResult(Activity.RESULT_CANCELED)
				}
				else -> infoTextView.visibility = View.GONE
			}
		})

		shareLogButton.setOnClickListener {
			val log = viewModel.logText.value ?: ""
			Intent(Intent.ACTION_SEND).also {
				it.type = "text/plain"
				it.putExtra(Intent.EXTRA_TEXT, log)
				startActivity(Intent.createChooser(it, resources.getString(R.string.action_share_log)))
			}
		}

		val registInfo = intent.getParcelableExtra<RegistInfo>(EXTRA_REGIST_INFO)
		if(registInfo == null)
		{
			finish()
			return
		}
		viewModel.start(registInfo)
	}

	override fun onStop()
	{
		super.onStop()
		viewModel.stop()
	}
}