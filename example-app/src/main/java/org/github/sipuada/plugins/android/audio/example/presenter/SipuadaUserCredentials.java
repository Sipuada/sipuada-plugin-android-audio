package org.github.sipuada.plugins.android.audio.example.presenter;

import android.os.Parcel;
import android.os.Parcelable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Credentials")
public class SipuadaUserCredentials extends Model implements Parcelable {

    @Column(name = "Username") protected String username;
    @Column(name = "PrimaryHost") protected String primaryHost;
    @Column(name = "Password") protected String password;

    public SipuadaUserCredentials() {}

    public SipuadaUserCredentials(String username, String primaryHost, String password) {
        this.username = username;
        this.primaryHost = primaryHost;
        this.password = password;
        SipuadaUserCredentials self = new Select().from(SipuadaUserCredentials.class)
                .where("Username = ? AND PrimaryHost = ?", username, primaryHost).executeSingle();
        if (self == null) {
            this.save();
        }
        else {
            self.username = username;
            self.primaryHost = primaryHost;
            self.password = password;
            self.save();
        }
    }

    protected SipuadaUserCredentials(Parcel in) {
        username = in.readString();
        primaryHost = in.readString();
        password = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(primaryHost);
        dest.writeString(password);
    }

    public static final Creator<SipuadaUserCredentials> CREATOR = new Creator<SipuadaUserCredentials>() {
        @Override
        public SipuadaUserCredentials createFromParcel(Parcel in) {
            return new SipuadaUserCredentials(in);
        }

        @Override
        public SipuadaUserCredentials[] newArray(int size) {
            return new SipuadaUserCredentials[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public String getPrimaryHost() {
        return primaryHost;
    }

    public String getPassword() {
        return password;
    }

}
