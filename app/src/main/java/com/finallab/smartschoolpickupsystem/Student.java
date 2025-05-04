package com.finallab.smartschoolpickupsystem;

public class Student {

        private String studentDocId;
        private String Sname;
        private String reg;
        private String studentClass;
        private String section;
        private String CNIC;
        private String image;


        public Student() {}

        public String getImage() {
                return image;
        }

        public void setImage(String image) {
                this.image = image;
        }

        // Getters and Setters


        public String getSname() {
                return Sname;
        }

        public void setSname(String Sname) {
                this.Sname = Sname;
        }

        public String getReg() {
                return reg;
        }

        public void setReg(String reg) {
                this.reg = reg;
        }

        public String getStudentClass() {
                return studentClass;
        }

        public void setStudentClass(String studentClass) {
                this.studentClass = studentClass;
        }

        public String getSection() {
                return section;
        }

        public void setSection(String section) {
                this.section = section;
        }

        public String getCNIC() {
                return CNIC;
        }

        public void setCNIC(String CNIC) {
                this.CNIC = CNIC;
        }

        public String getStudentDocId() {
                return studentDocId;
        }

        public void setStudentDocId(String studentDocId) {
                this.studentDocId = studentDocId;
        }
}