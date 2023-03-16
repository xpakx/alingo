import { CourseData } from "./course-data";
import { CourseDetails } from "./course-details";

export interface CourseList {
    page: number,
    size: number,
    totalSize: number,
    courses: CourseDetails[]
}