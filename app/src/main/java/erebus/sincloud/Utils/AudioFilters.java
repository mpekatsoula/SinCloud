package erebus.sincloud.Utils;

import android.app.ProgressDialog;

import erebus.sincloud.Activities.RecordSinActivity;
import erebus.sincloud.Helpers.AudioEffect;
import erebus.sincloud.R;
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class AudioFilters
{
     public String changePlaybackSpeed(String fileName, RecordSinActivity recordSinActivity, AudioEffect audioEffect)
     {
         String factor = "";
         switch (audioEffect)
         {
             case FAST:
                 factor = "*0.5";
                 break;
             case SLOW:
                 factor = "*0.28";
                 break;
             default:
                 break;
         }
         String convertedFile = fileName.replace(".m4a","_converted.m4a");

         String[] args = {
                  "-i",
                  fileName,
                  "-af",
                  "asetrate=44100"+factor,
                  convertedFile};

         if (FFmpeg.getInstance(recordSinActivity).isSupported())
         {

             // Show waiting dialog
             final ProgressDialog uploadingDialog = ProgressDialog.show(recordSinActivity, recordSinActivity.getString(R.string.applying_filter_title),
                     recordSinActivity.getString(R.string.apply_filter_text), true);
             uploadingDialog.create();

             FFmpeg ffmpeg = FFmpeg.getInstance(recordSinActivity);
             ffmpeg.execute(args, new ExecuteBinaryResponseHandler()
             {

                 @Override
                 public void onStart()
                 {

                 }

                 @Override
                 public void onProgress(String message)
                 {

                 }

                 @Override
                 public void onFailure(String message)
                 {

                 }

                 @Override
                 public void onSuccess(String message)
                 {

                 }

                 @Override
                 public void onFinish()
                 {
                     uploadingDialog.dismiss();
                 }
             });
         }
         else
         {
         }

         return convertedFile;
     }
}
