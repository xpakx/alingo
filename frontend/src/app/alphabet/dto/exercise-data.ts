import { ExerciseDetails } from "./exercise-details";

export interface ExerciseData extends ExerciseDetails {
    course: {
        id: number,
        name: String
    }
}