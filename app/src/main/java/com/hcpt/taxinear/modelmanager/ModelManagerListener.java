package com.hcpt.taxinear.modelmanager;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

public interface ModelManagerListener {
    //public void onError(VolleyError error);
    public void onError();

    public void onSuccess(String json);
}
