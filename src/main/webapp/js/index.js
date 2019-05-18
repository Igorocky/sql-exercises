'use strict';

const re = React.createElement;

const {
    Button,
    Paper,
    colors,
    createMuiTheme,
    CssBaseline,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Icon,
    MuiThemeProvider,
    Typography,
    withStyles,
} = window['material-ui'];

const pages = {}
pages["SqlExercisesList"] = SqlExercisesList
pages["SqlExerciseFullDescription"] = SqlExerciseFullDescription

ReactDOM.render(
    re(pages[pageType], {pageData:pageData}),
    document.getElementById('react-container')
)