/**
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.hardware.radio;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

/**
 * Implements the RadioTuner interface by forwarding calls to radio service.
 */
class TunerAdapter extends RadioTuner {
    private static final String TAG = "radio.TunerAdapter";

    @NonNull private final ITuner mTuner;
    private boolean mIsClosed = false;

    TunerAdapter(ITuner tuner) {
        if (tuner == null) {
            throw new NullPointerException();
        }
        mTuner = tuner;
    }

    @Override
    public void close() {
        synchronized (mTuner) {
            if (mIsClosed) {
                Log.d(TAG, "Tuner is already closed");
                return;
            }
            mIsClosed = true;
        }
        try {
            mTuner.close();
        } catch (RemoteException e) {
            Log.e(TAG, "Exception trying to close tuner", e);
        }
    }

    @Override
    public int setConfiguration(RadioManager.BandConfig config) {
        try {
            mTuner.setConfiguration(config);
            return RadioManager.STATUS_OK;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Can't set configuration", e);
            return RadioManager.STATUS_BAD_VALUE;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
    }

    @Override
    public int getConfiguration(RadioManager.BandConfig[] config) {
        if (config == null || config.length != 1) {
            throw new IllegalArgumentException("The argument must be an array of length 1");
        }
        try {
            config[0] = mTuner.getConfiguration();
            return RadioManager.STATUS_OK;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
    }

    @Override
    public int setMute(boolean mute) {
        try {
            mTuner.setMuted(mute);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't set muted", e);
            return RadioManager.STATUS_ERROR;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
        return RadioManager.STATUS_OK;
    }

    @Override
    public boolean getMute() {
        try {
            return mTuner.isMuted();
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return true;
        }
    }

    @Override
    public int step(int direction, boolean skipSubChannel) {
        try {
            mTuner.step(direction == RadioTuner.DIRECTION_DOWN, skipSubChannel);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't step", e);
            return RadioManager.STATUS_INVALID_OPERATION;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
        return RadioManager.STATUS_OK;
    }

    @Override
    public int scan(int direction, boolean skipSubChannel) {
        try {
            mTuner.scan(direction == RadioTuner.DIRECTION_DOWN, skipSubChannel);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't scan", e);
            return RadioManager.STATUS_INVALID_OPERATION;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
        return RadioManager.STATUS_OK;
    }

    @Override
    public int tune(int channel, int subChannel) {
        try {
            mTuner.tune(channel, subChannel);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't tune", e);
            return RadioManager.STATUS_INVALID_OPERATION;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Can't tune", e);
            return RadioManager.STATUS_BAD_VALUE;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
        return RadioManager.STATUS_OK;
    }

    @Override
    public int cancel() {
        try {
            mTuner.cancel();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Can't cancel", e);
            return RadioManager.STATUS_INVALID_OPERATION;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
        return RadioManager.STATUS_OK;
    }

    @Override
    public int getProgramInformation(RadioManager.ProgramInfo[] info) {
        if (info == null || info.length != 1) {
            throw new IllegalArgumentException("The argument must be an array of length 1");
        }
        try {
            info[0] = mTuner.getProgramInformation();
            return RadioManager.STATUS_OK;
        } catch (RemoteException e) {
            Log.e(TAG, "service died", e);
            return RadioManager.STATUS_DEAD_OBJECT;
        }
    }

    @Override
    public boolean startBackgroundScan() {
        try {
            return mTuner.startBackgroundScan();
        } catch (RemoteException e) {
            throw new RuntimeException("service died", e);
        }
    }

    @Override
    public @NonNull List<RadioManager.ProgramInfo> getProgramList(@Nullable String filter) {
        try {
            return mTuner.getProgramList(filter);
        } catch (RemoteException e) {
            throw new RuntimeException("service died", e);
        }
    }

    @Override
    public boolean isAnalogForced() {
        try {
            return mTuner.isAnalogForced();
        } catch (RemoteException e) {
            throw new RuntimeException("service died", e);
        }
    }

    @Override
    public void setAnalogForced(boolean isForced) {
        try {
            mTuner.setAnalogForced(isForced);
        } catch (RemoteException e) {
            throw new RuntimeException("service died", e);
        }
    }

    @Override
    public boolean isAntennaConnected() {
        try {
            return mTuner.isAntennaConnected();
        } catch (RemoteException e) {
            throw new RuntimeException("service died", e);
        }
    }

    @Override
    public boolean hasControl() {
        // TODO(b/36863239): forward to mTuner
        throw new RuntimeException("Not implemented");
    }
}
