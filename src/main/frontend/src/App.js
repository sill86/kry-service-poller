import React, { Component } from "react";
import './App.css';
import Service from './Service';
import ServiceForm from "./ServiceForm";

class App extends Component {
  render() {
    return (
      <div className="App">
        <h1>
          Services
        </h1>
        <div>
          <ServiceForm/>
        </div>
        <Service/>
      </div>
    )
  }
}
export default App;
