import { CourseDetails } from "./course-details";

export interface CourseData extends CourseDetails {
    language: {
        id: number,
        name: String
    }
}