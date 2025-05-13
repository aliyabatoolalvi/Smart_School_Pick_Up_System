package com.finallab.smartschoolpickupsystem;

import com.google.firebase.Timestamp;

public class GuardianNotifiaction {

        private String title;
        private String body;
        private Timestamp timestamp;
        private boolean seen;
        private String id;

        public GuardianNotifiaction() {}

        public String getTitle() { return title; }
        public String getBody() { return body; }
        public Timestamp getTimestamp() { return timestamp; }
        public boolean isSeen() { return seen; }
        public void setSeen(boolean seen) {
                this.seen = seen;
        }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

}


