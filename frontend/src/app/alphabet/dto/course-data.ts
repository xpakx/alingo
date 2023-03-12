import { CourseDetails } from "./course-details";

export interface CourseData extends CourseDetails {
    premium: boolean,
    language: {
        id: number,
        name: String
    }
}