package com.pdfreader.freecodedfapp.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.pdfreader.freecodedfapp.dao.ConfigDao;
import com.pdfreader.freecodedfapp.db.DatabaseClient;
import com.pdfreader.freecodedfapp.models.ConfigModel;

public class ConfigRepository {
    private final ConfigDao configDao;

    public ConfigRepository(Application application) {
        this.configDao = DatabaseClient.getInstance(application).getAppDatabase().configDao();
    }

    public ConfigModel getConfigByKey(int i) {
        try {
            return (ConfigModel) new GetConfigByKey(this.configDao, i).execute(new Void[0]).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void incrementConfigValue(int i) {
        DatabaseClient.databaseWriteExecutor.execute(new Runnable() {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = i;
            }

            public final void run() {
                ConfigRepository.this.lambda$incrementConfigValue$0$ConfigRepository(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$incrementConfigValue$0$ConfigRepository(int i) {
        this.configDao.incrementConfigValue(i);
    }

    public /* synthetic */ void lambda$setRewarded$1$ConfigRepository(int i, int i2) {
        this.configDao.setRewarded(i, i2);
    }

    public void setRewarded(int i, int i2) {
        DatabaseClient.databaseWriteExecutor.execute(new Runnable() {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = i;
                this.f$2 = i2;
            }

            public final void run() {
                ConfigRepository.this.lambda$setRewarded$1$ConfigRepository(this.f$1, this.f$2);
            }
        });
    }

    private class GetConfigByKey extends AsyncTask<Void, Void, ConfigModel> {
        private final ConfigDao configDao;
        private final int key;

        GetConfigByKey(ConfigDao configDao2, int i) {
            this.configDao = configDao2;
            this.key = i;
        }

        public ConfigModel doInBackground(Void... voidArr) {
            return this.configDao.getConfigBykey(this.key);
        }
    }
}
