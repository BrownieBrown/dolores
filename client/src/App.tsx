import React from 'react';
import './App.css';
import { LocomotiveScrollProvider } from "react-locomotive-scroll";
import { useRef } from "react";

function App() {
    const ref = useRef(null);

    const options = {
        smooth: true,
    }

    return (
        <LocomotiveScrollProvider options={options} containerRef={ref}>
            <main data-scroll-container ref={ref}>
                <section className="intro" data-scroll-section>
                    <h1>Welcome To Dolores</h1>
                </section>
                <section className="contents" data-scroll-section>
                    <h1
                        data-scroll
                        data-scroll-direction="horizontal"
                        data-scroll-speed="9"
                    >
                        That which is real is irreplaceable.
                    </h1>
                    <h1
                        data-scroll
                        data-scroll-direction="vertical"
                        data-scroll-speed="9" // Values provided here affect the animations
                    >
                        Carpe Diem
                    </h1>
                </section>
                <section className="footer" data-scroll-section>
                    <h1>I don’t wanna be in a story. All I want is to not look forward or back. I just wanna be… in the moment I’m in.</h1>
                </section>
            </main>
        </LocomotiveScrollProvider>
    );
}

export default App;
