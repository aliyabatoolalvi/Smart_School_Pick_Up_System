package com.finallab.smartschoolpickupsystem;

public class Feedback {
        private String guardianCNIC;
        private String feedbackText;
        private com.google.firebase.Timestamp timestamp;
        public Feedback() {
        }

        public Feedback(String guardianCNIC, String feedbackText, com.google.firebase.Timestamp timestamp) {
            this.guardianCNIC = guardianCNIC;
            this.feedbackText = feedbackText;
            this.timestamp = timestamp;
        }

        // Getters
        public String getGuardianCNIC() {
            return guardianCNIC;
        }

        public String getFeedbackText() {
            return feedbackText;
        }

        public com.google.firebase.Timestamp getTimestamp() {
            return timestamp;
        }

        // Setters
        public void setGuardianCNIC(String guardianCNIC) {
            this.guardianCNIC = guardianCNIC;
        }

        public void setFeedbackText(String feedbackText) {
            this.feedbackText = feedbackText;
        }

        public void setTimestamp(com.google.firebase.Timestamp timestamp) {
            this.timestamp = timestamp;}
    }
