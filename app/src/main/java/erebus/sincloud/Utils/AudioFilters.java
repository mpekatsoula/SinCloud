package erebus.sincloud.Utils;

import android.app.ProgressDialog;
import android.util.Log;

import erebus.sincloud.Activities.RecordSinActivity;
import erebus.sincloud.Helpers.AudioEffect;
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

         Log.d("1337", "asetrate=44100"+factor);

         if (FFmpeg.getInstance(recordSinActivity).isSupported())
         {

             // Show waiting dialog
             final ProgressDialog uploadingDialog = ProgressDialog.show(recordSinActivity, "Applying filter",
                     "Please wait...", true);
             uploadingDialog.create();

             FFmpeg ffmpeg = FFmpeg.getInstance(recordSinActivity);
             Log.d("1337", "supported");
             ffmpeg.execute(args, new ExecuteBinaryResponseHandler()
             {

                 @Override
                 public void onStart()
                 {
                     Log.d("1337", "onStart");
                 }

                 @Override
                 public void onProgress(String message)
                 {
                     Log.d("1337", "onProgress " + message);
                 }

                 @Override
                 public void onFailure(String message)
                 {
                     Log.d("1337", "onFailure " + message);
                 }

                 @Override
                 public void onSuccess(String message)
                 {
                     Log.d("1337", "onSuccess " + message);
                 }

                 @Override
                 public void onFinish()
                 {
                     Log.d("1337", "onFinish");
                     uploadingDialog.dismiss();
                 }
             });
         }
         else
         {
                 Log.d("1337", " Not supported");
         }

         return convertedFile;
     }
}
